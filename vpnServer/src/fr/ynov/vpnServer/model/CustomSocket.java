package fr.ynov.vpnServer.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ynov.vpnModel.model.CryptedMessage;
import fr.ynov.vpnModel.model.Message;
import fr.ynov.vpnModel.model.MessageType;
import fr.ynov.vpnModel.model.Origin;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class CustomSocket {

    private final Socket socket;
    private final List<Message> messages = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final UUID uuid;
    private SecretKey publicKey;
    private String name;

    public CustomSocket(Socket socket) throws IOException {
        this.socket = socket;
        this.uuid = UUID.randomUUID();
    }

    public SecretKey getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(SecretKey publicKey) {
        this.publicKey = publicKey;
    }

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

    public void setName(String name) {
        this.name = name;
    }

    public void sendMessage(String content, Boolean crypted) throws IOException, AssertionError, NoSuchAlgorithmException, NoSuchPaddingException {
        Message msg = new Message(content, Origin.SERVER, crypted, MessageType.MESSAGE);
        this.messages.add(msg);
        if (crypted) {
            CryptedMessage cMsg = new CryptedMessage(content, Origin.SERVER, true, MessageType.MESSAGE);
            cMsg.encrypt(this.publicKey);
            msg = cMsg;
        }
        sendMessage(msg);
    }

    public void sendMessage(Message msg) {
        if (msg == null) {
            System.err.println("Unable to send message because message is null");
            return;
        }
        try {
            PrintWriter output = new PrintWriter(this.socket.getOutputStream(), true);
            output.println(msg.getJSON());
        } catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
        }
    }

    public Message listenForMessage() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            Message msg;
            String line;
            while (this.socket.isConnected() && (line = in.readLine()) != null) {
                msg = mapper.readValue(line, Message.class);
                return msg;
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.err.println("Unable to listen for messages: " + e.getMessage());
            }
        }
        return null;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public List<Message> getMessages() {
        return this.messages;
    }

    public void addMessage(Message msg) {
        this.messages.add(msg);
    }

    @Override
    public String toString() {
        return getName();
    }
}
