package fr.ynov.vpnClient.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ynov.vpnModel.model.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;


public class ClientSocket extends Socket implements EventInterface {

    private final List<Message> messages = new ArrayList<>();
    private final UUID uuid = UUID.randomUUID();
    private final String name;
    private SecretKey privateKey;
    private SecretKey publicKey;
    private SecretKey serverKey;
    private BiConsumer<ClientSocket, Message> onMessage;
    private BiConsumer<ClientSocket, ConfigurationMessage> onMessageConfiguration;
    private Function<ClientSocket, Void> onConnect;
    private Function<ClientSocket, Void> onDisconnect;
    private Function<ClientSocket, Void> onError;
    private String serverName;

    private final ObjectMapper mapper = new ObjectMapper();

    public ClientSocket(String host, int port, String name) throws IOException {
        // Connect to the socket with a timeout of 5s
        connect(new InetSocketAddress(host, port), 5000);
        this.name = name;
        // Start a new thread for listening message
        new Thread(this::listenMessage).start();
    }

    // This method is used to set the privateKey of the socket
    // It's also define the publicKey based on the privateKey sended
    public void setPrivateKey(SecretKey privateKey) {
        this.publicKey = privateKey;
        this.privateKey = privateKey;
    }

    // This method is used to send the public key to the server and ask him to return his public key
    // Allowing an encrypted conversation
    public void askServerKey() {
        String pubKey = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
        ConfigurationMessage confMessage = new ConfigurationMessage(pubKey, Origin.CLIENT, false, MessageType.CONFIG, SocketConfiguration.GET_PUBLIC_KEY);
        this.messages.add(confMessage);
        sendMessage(confMessage);
    }

    // This method is used to send the socket Username to the server
    public void sendName() {
        ConfigurationMessage confMessage = new ConfigurationMessage(name, Origin.CLIENT, false, MessageType.CONFIG, SocketConfiguration.SET_NAME);
        this.messages.add(confMessage);
        confMessage.encrypt(this.serverKey);
        sendMessage(confMessage);
    }

    public List<Message> getMessages() {
        return this.messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    // This method is used to listen for new message
    private void listenMessage() {
        try {
            // Open a new BufferedReader on the socket inputStream
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getInputStream()));
            // Define null variable used later
            Message msg;
            String line;
            // While the socket is connected and the readed line is not null
            while (this.isConnected() && (line = in.readLine()) != null) {
                // Parse th line into a new Message object using Jackson
                msg = mapper.readValue(line, Message.class);

                // If it is a configuration message, send it to the handling configuration method
                if (msg.getType() == MessageType.CONFIG) {
                    // Send it casted into configurationMessage
                    parseConfigFromMessage((ConfigurationMessage) msg);
                    continue;
                }
                // If it is crypted, decrypt it
                if (msg.isCrypted()) {
                    if (this.privateKey == null) {
                        this.messages.add(msg);
                        continue;
                    }

                    CryptedMessage cMsg = (CryptedMessage) msg;
                    cMsg.decrypt(this.privateKey);

                    if (cMsg.getContent() == null) {
                        this.askServerKey();
                    }
                }
                // Store the message and send a new onMessage event
                this.messages.add(msg);
                onMessage(this, msg);
            }
            // If the readed line is null, it's mean we reach EOF, so the socket is bad detected as open
            // So we close it and send a disconnect event
            if(in.readLine() == null) {
                this.close();
                onDisconnect(this);
                throw new IOException("Socket Closed");
            }
        } catch (IOException e) {
            if (!this.isClosed()) {
                System.err.println("Socket exception, maybe unable to read input stream: " + e.getMessage());
            }
        }
    }

    // This method is used to create and send a new message
    public void sendMessage(String content, Boolean crypted) {
        // Creating the message from the given parameter
        Message msg = new Message(content, Origin.CLIENT, crypted, MessageType.MESSAGE);
        // Adding the message to the list before it's encrypted
        this.messages.add(msg);
        // If the message should be encrypted, encrypt it with the server key
        if (crypted) {
            CryptedMessage cMsg = new CryptedMessage(content, Origin.CLIENT, true, MessageType.MESSAGE);
            cMsg.encrypt(this.serverKey);
            msg = cMsg;
        }
        // send the message to the server
        sendMessage(msg);
    }

    // This method send message to the server
    public void sendMessage(Message msg) {
        // Exit conditions
        if (msg == null) {
            System.err.println("Error sending message, cannot send void message");
            return;
        }
        try {
            // Open a new PrintWriter with autoFlush
            PrintWriter out = new PrintWriter(getOutputStream(), true);
            // Print the message inside the output stream after it being parsed into JSON using Jackson
            out.println(msg.getJSON());
        } catch (IOException e) {
            if (!isClosed()) {
                System.err.println("Error sending message: " + e.getMessage());
                onError(this);
            }
        }
    }

    // This method handle configuration message
    private void parseConfigFromMessage(ConfigurationMessage confMessage) {
        // If the message is crypted, decrypt it
        if (confMessage.isCrypted()) {
            confMessage.decrypt(this.privateKey);
        }
        // If the content is null,
        // it means an error occure when decrypting
        // So we restart the key trade with the server and return
        if (confMessage.getContent() == null) {
            askServerKey();
            return;
        }
        // Adding the configuration message to the message list
        addMessage(confMessage);

        switch (confMessage.getConfiguration()) {
            // If the configuration is that the server send his key back
            case SEND_PUBLIC_KEY -> {
                // parse the key and store it
                this.serverKey = new SecretKeySpec(Base64.getDecoder().decode(confMessage.getContent()), "AES");
                // send socket username to the server
                sendName();
            }
            // If the config is that the server send his name, we store it
            case SET_NAME -> this.serverName = confMessage.getContent();
        }
        // send a new configuration message event
        onMessageConfiguration(this, confMessage);

    }

    // This method override the classic toString method
    // The new method return the serverName or socket uuid with state if it is closed
    @Override
    public String toString() {

        String res = "";
        if (serverName == null) {
            res += uuid.toString();
        } else {
            res += serverName;
        }
        if (this.isClosed()) {
            res += " (closed)";
        }
        return res;
    }

    // All event sender method
    @Override
    public void onMessage(ClientSocket socket, Message message) {
        if (onMessage != null) {
            onMessage.accept(socket, message);
        }
    }

    @Override
    public void onMessageConfiguration(ClientSocket socket, ConfigurationMessage message) {
        if (onMessageConfiguration != null) {
            onMessageConfiguration.accept(socket, message);
        }
    }

    @Override
    public void onConnect(ClientSocket socket) {
        if (onConnect != null) {
            onConnect.apply(socket);
        }
    }

    @Override
    public void onDisconnect(ClientSocket socket) {
        if (onDisconnect != null) {
            onDisconnect.apply(socket);
        }
    }

    @Override
    public void onError(ClientSocket socket) {
        if (onError != null) {
            onError.apply(socket);
        }
    }

    // All method used to set event listener
    @Override
    public void setOnMessage(BiConsumer<ClientSocket, Message> onMessage) {
        this.onMessage = onMessage;
    }

    @Override
    public void setOnMessageConfiguration(BiConsumer<ClientSocket, ConfigurationMessage> onMessageConfiguration) {
        this.onMessageConfiguration = onMessageConfiguration;
    }

    @Override
    public void setOnConnect(Function<ClientSocket, Void> onConnect) {
        this.onConnect = onConnect;
    }

    @Override
    public void setOnDisconnect(Function<ClientSocket, Void> onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    @Override
    public void setOnError(Function<ClientSocket, Void> onError) {
        this.onError = onError;
    }
}