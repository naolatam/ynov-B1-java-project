package fr.ynov.vpnServer.model;

import fr.ynov.vpnModel.model.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * CustomServerSocket is a server socket implementation that manages multiple client connections.
 * It handles encrypted messages, configuration messages, and client connection events.
 */
public class CustomServerSocket extends ServerSocket implements EncryptDecryptInterface, EventInterface {

    private final List<CustomSocket> clients = new ArrayList<>();
    private final String serverName;
    private SecretKey privateKey;
    private SecretKey publicKey;
    private BiConsumer<CustomSocket, Message> onMessage;
    private BiConsumer<CustomSocket, ConfigurationMessage> onMessageConfiguration;
    private Function<CustomSocket, Void> onConnect;
    private Function<CustomSocket, Void> onDisconnect;
    private Function<CustomSocket, Void> onError;

    /**
     * Constructs a CustomServerSocket bound to a specific port.
     *
     * @param port the port number to bind the server socket.
     * @param name the name of the server.
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    public CustomServerSocket(int port, String name) throws IOException {
        super(port);
        this.serverName = name;
        // Start a new thread to listen incoming connection
        new Thread(this::handleConnection).start();
    }

    /**
     * Sets the private and derive the public key for encryption.
     *
     * @param privateKey the private key for encryption.
     */
    public void setPrivateKey(SecretKey privateKey) {
        this.publicKey = privateKey;
        this.privateKey = privateKey;
    }

    /**
     * Sends the server's public key to a client for secure communication.
     *
     * @param socket the {@link CustomSocket} to send the public key to.
     */
    public void sendServerKey(CustomSocket socket) {
        // Encode the public key into base64
        String pubKey = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
        // Encrypt it with the client public key
        pubKey = this.encrypt(socket.getPublicKey(), pubKey);
        ConfigurationMessage confMessage = new ConfigurationMessage(
                pubKey, Origin.SERVER, true, MessageType.CONFIG,
                SocketConfiguration.SEND_PUBLIC_KEY);
        // Add it to message history and send it
        socket.sendMessage(confMessage);
        socket.addMessage(confMessage);
    }


    /**
     * Sends the server's name to a client.
     *
     * @param socket the client socket to send the name to.
     */
    public void sendName(CustomSocket socket) {
        // Create a configuration message for sending the server's name
        ConfigurationMessage confMessage = new ConfigurationMessage(
                serverName, Origin.SERVER, true, MessageType.CONFIG,
                SocketConfiguration.SET_NAME);
        // Add the message to history before it encrypted
        socket.addMessage(confMessage);
        // Encrypt it and send it encrypted
        confMessage.encrypt(socket.getPublicKey());
        socket.sendMessage(confMessage);

    }

    /**
     * Handles incoming client connections and starts a new thread for each connected client to listen message.
     */
    private void handleConnection() {
        // Loop while the server is started
        while (this.isBound()) {
            try {
                // Accept the connection
                Socket s = this.accept();
                // Create a new custom socket from it
                CustomSocket socket = new CustomSocket(s);
                clients.add(socket);
                // Send an onConnect event
                onConnect(socket);
                // Start a new thread to listen message on this socket
                // This avoid blocking script execution
                new Thread(() -> {
                    try {
                        this.handleMessage(socket);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Handles messages from a connected client.
     *
     * @param socket the client socket to listen for messages.
     */
    private void handleMessage(CustomSocket socket) {
        while (socket != null && !socket.getSocket().isClosed() ) {
            // Listening for new message
            Message msg = socket.listenForMessage();
            // If there is a message, handle it
            if (msg != null) {
                if (msg.getType() == MessageType.CONFIG) {
                    ConfigurationMessage confMessage = (ConfigurationMessage) msg;
                    parseConfigFromMessage(socket, confMessage);
                    continue;
                }
                if (msg.isCrypted()) {
                    CryptedMessage cryptedMessage = (CryptedMessage) msg;
                    cryptedMessage.decrypt(this.privateKey);
                    msg = cryptedMessage;
                }
                // Add the message to the history and send onMessage event
                socket.addMessage(msg);
                onMessage(socket, msg);
            }
        }
        // If there is no socket, return an error
        if(socket.getSocket() == null) {
            onError(socket);
            return;
        }
        // If the loop end, check if the socket is closed, then send a onDisconnect event
        if (socket.getSocket().isClosed()) {
            onDisconnect(socket);
        }
    }

    /**
     * Parses a configuration message received from a client.
     *
     * @param socket the client socket.
     * @param confMessage the {@link ConfigurationMessage}.
     */
    private void parseConfigFromMessage(CustomSocket socket, ConfigurationMessage confMessage) {
        if (confMessage.getOrigin() != Origin.CLIENT) {
            return;
        }
        if (confMessage.isCrypted()) {
            confMessage.decrypt(this.privateKey);
        }
        if (confMessage.getContent() == null) {
            sendServerKey(socket);
            return;
        }
        socket.addMessage(confMessage);
        switch (confMessage.getConfiguration()) {
            case GET_PUBLIC_KEY -> {
                SecretKey clientPubKey = new SecretKeySpec(Base64.getDecoder().decode(confMessage.getContent()), "AES");
                socket.setPublicKey(clientPubKey);
                this.sendServerKey(socket);
            }
            case SET_NAME -> {
                socket.setName(confMessage.getContent());
                sendName(socket);
            }
        }
        onMessageConfiguration(socket, confMessage);
    }


    // Event listener
    @Override
    public void onMessage(CustomSocket cs, Message message) {
        if (onMessage != null) {
            this.onMessage.accept(cs, message);
        }
    }

    @Override
    public void onMessageConfiguration(CustomSocket cs, ConfigurationMessage message) {
        if (onMessageConfiguration != null) {
            this.onMessageConfiguration.accept(cs, message);
        }
    }

    @Override
    public void onConnect(CustomSocket socket) {
        if (onConnect != null) {
            this.onConnect.apply(socket);
        }
    }

    @Override
    public void onDisconnect(CustomSocket socket) {
        this.clients.remove(socket);
        if (onDisconnect != null) {
            this.onDisconnect.apply(socket);
        }
    }

    @Override
    public void onError(CustomSocket socket) {
        if (onError != null) {
            this.onError.apply(socket);
        }
    }

    // Setter for event listener.
    @Override
    public void setOnMessage(BiConsumer<CustomSocket, Message> onMessage) {
        this.onMessage = onMessage;
    }

    @Override
    public void setOnMessageConfiguration(BiConsumer<CustomSocket, ConfigurationMessage> onMessageConfiguration) {
        this.onMessageConfiguration = onMessageConfiguration;
    }

    @Override
    public void setOnConnect(Function<CustomSocket, Void> onConnect) {
        this.onConnect = onConnect;
    }

    @Override
    public void setOnDisconnect(Function<CustomSocket, Void> onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    @Override
    public void setOnError(Function<CustomSocket, Void> onError) {
        this.onError = onError;
    }
}
