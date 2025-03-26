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


    public ClientSocket(String host, int port, String name) throws IOException {
        connect(new InetSocketAddress(host, port), 5000);
        this.name = name;
        new Thread(this::listenMessage).start();
    }

    public void setPrivateKey(SecretKey privateKey) {
        this.publicKey = privateKey;
        this.privateKey = privateKey;
    }

    public void askServerKey() {
        String pubKey = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
        ConfigurationMessage confMessage = new ConfigurationMessage(pubKey, Origin.CLIENT, false, MessageType.CONFIG, SocketConfiguration.GET_PUBLIC_KEY);
        this.messages.add(confMessage);
        sendMessage(confMessage);
    }

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

    private void listenMessage() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getInputStream()));
            Message msg;
            String line;
            ObjectMapper mapper = new ObjectMapper();
            while (this.isConnected() && (line = in.readLine()) != null) {
                msg = mapper.readValue(line, Message.class);

                if (msg.getType() == MessageType.CONFIG) {
                    parseConfigFromMessage((ConfigurationMessage) msg);
                    continue;
                }
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
                this.messages.add(msg);
                onMessage(this, msg);
            }
        } catch (IOException e) {
            if (!this.isClosed()) {
                System.err.println("Socket exception, maybe unable to read input stream: " + e.getMessage());
            }
        }
    }

    public void sendMessage(String content, Boolean crypted) {
        Message msg = new Message(content, Origin.CLIENT, crypted, MessageType.MESSAGE);
        this.messages.add(msg);
        if (crypted) {
            CryptedMessage cMsg = new CryptedMessage(content, Origin.CLIENT, true, MessageType.MESSAGE);
            cMsg.encrypt(this.serverKey);
            msg = cMsg;
        }
        sendMessage(msg);
    }

    public void sendMessage(Message msg) {
        try {
            PrintWriter out = new PrintWriter(getOutputStream(), true);
            if (msg == null) {
                System.err.println("Error sending message, cannot send void message");
                return;
            }
            out.println(msg.getJSON());
        } catch (IOException e) {
            if (!isClosed()) {
                System.err.println("Error sending message: " + e.getMessage());
                onError(this);
            }
        }
    }

    private void parseConfigFromMessage(ConfigurationMessage confMessage) {
        if (confMessage.isCrypted()) {
            confMessage.decrypt(this.privateKey);
        }
        if (confMessage.getContent() == null) {
            askServerKey();
            return;
        }
        this.messages.add(confMessage);
        if (confMessage.getConfiguration() == SocketConfiguration.SEND_PUBLIC_KEY) {
            this.serverKey = new SecretKeySpec(Base64.getDecoder().decode(confMessage.getContent()), "AES");
            sendName();
        }
        if (confMessage.getConfiguration() == SocketConfiguration.SET_NAME) {
            this.serverName = confMessage.getContent();
        }
        onMessageConfiguration(this, confMessage);

    }

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