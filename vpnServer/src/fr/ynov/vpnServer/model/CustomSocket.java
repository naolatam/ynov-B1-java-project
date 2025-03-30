package fr.ynov.vpnServer.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ynov.vpnModel.model.CryptedMessage;
import fr.ynov.vpnModel.model.Message;
import fr.ynov.vpnModel.model.MessageType;
import fr.ynov.vpnModel.model.Origin;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a custom socket that manages communication between a client and server.
 * This class handles message sending, receiving, encryption, and socket identification.
 */
public class CustomSocket {

    private final Socket socket;
    private final List<Message> messages = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final UUID uuid;
    private SecretKey publicKey;
    private String name;

    /**
     * Constructs a CustomSocket instance with the given socket.
     *
     * @param socket The socket representing the connection.
     */
    public CustomSocket(Socket socket) {
        this.socket = socket;
        this.uuid = UUID.randomUUID();
    }

    /**
     * Gets the client public key used for encryption.
     *
     * @return The public key.
     */
    public SecretKey getPublicKey() {
        return this.publicKey;
    }

    /**
     * Sets the public key for encryption.
     *
     * @param publicKey The public key to set.
     */
    public void setPublicKey(SecretKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Gets the name of the socket, including its UUID and status.
     *
     * @return The name of the socket.
     */
    public String getName() {
        StringBuilder res = new StringBuilder();
        res.append(Objects.requireNonNullElse(name, uuid.toString()));

        if (name != null && !name.isEmpty()) {
            res.append(" (").append(uuid.toString().split("-")[0]).append(")");
        }

        if (socket.isClosed()) {
            res.append(" (closed)");
        }
        return res.toString();
    }

    /**
     * Sets the name of the socket.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Create and send a message with optional encryption.
     *
     * @param content The content of the message.
     * @param encrypted Whether the message should be encrypted.
     */
    public void sendMessage(String content, Boolean encrypted) {
        // Create a message and add it to the history before encrypting it
        Message msg = new Message(content, Origin.SERVER, encrypted, MessageType.MESSAGE);
        this.messages.add(msg);
        if (encrypted) {
            CryptedMessage cMsg = new CryptedMessage(content, Origin.SERVER, true, MessageType.MESSAGE);
            cMsg.encrypt(this.publicKey);
            msg = cMsg;
        }
        // Send the message
        sendMessage(msg);
    }

    /**
     * Sends a message over the socket.
     *
     * @param msg The message to send.
     */
    public void sendMessage(Message msg) {
        if (msg == null) {
            System.err.println("Unable to send message because message is null");
            return;
        }
        try {
            // Open a PrintWriter on the socket output stream
            // This could be set as an attributs to avoid setting up a new one for each sended message
            PrintWriter output = new PrintWriter(this.socket.getOutputStream(), true);
            output.println(msg.getJSON());
        } catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
        }
    }

    /**
     * Listens for incoming messages from the socket.
     *
     * @return The received message or null if an error occurs.
     */
    public Message listenForMessage() {
        try {
            // Set a new bufferedReader for reading the socket input stream
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            // Init next used variable
            Message msg;
            String line = in.readLine();
            // If the line was read correctly, parse the string into a message class and return it
            if (!this.socket.isClosed() && line != null) {
                msg = mapper.readValue(line, Message.class);
                return msg;
            }
            // If no line can be read, so null, this mean the inputStream reach END-OF-FILE,
            // so it's close the connection
            if (in.readLine() == null) {
                this.socket.close();
                throw new IOException("Socket Closed");
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.err.println("Unable to listen for messages: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Gets the underlying socket.
     *
     * @return The socket.
     */
    public Socket getSocket() {
        return this.socket;
    }

    /**
     * Gets the list of messages associated with this socket.
     *
     * @return A list of messages.
     */
    public List<Message> getMessages() {
        return this.messages;
    }

    /**
     * Adds a message to the message list.
     *
     * @param msg The message to add.
     */
    public void addMessage(Message msg) {
        this.messages.add(msg);
    }

    /**
     * Returns the string representation of the socket, including its name.
     *
     * @return The string representation of the socket.
     */
    @Override
    public String toString() {
        return getName();
    }
}
