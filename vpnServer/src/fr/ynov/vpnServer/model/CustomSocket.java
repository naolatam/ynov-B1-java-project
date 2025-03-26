package fr.ynov.vpnServer.model;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.ynov.vpnModel.model.Message;
import fr.ynov.vpnModel.model.CryptedMessage;
import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.MessageType;
import fr.ynov.vpnModel.model.SocketConfiguration;
import fr.ynov.vpnModel.model.Origin;
import fr.ynov.vpnModel.model.EncryptDecryptInterface;


public class CustomSocket {

    private Socket socket;

    private SecretKey publicKey;

    private final List<Message> messages = new ArrayList<>();

    private String name;
    private UUID uuid;

    public CustomSocket(Socket socket) throws IOException {
        this.socket = socket;
        this.uuid = UUID.randomUUID();
    }

    public void setPublicKey(SecretKey publicKey) {
        this.publicKey = publicKey;
    }

    public SecretKey getPublicKey() {
        return this.publicKey;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        String res = "";
        if(name == "" || name == null) {
            res += getUuid().toString();
        }else {
            res += name + "(" + getUuid().toString().split("-")[0]+ ")";
        }
        if(socket.isClosed()) {
            res += " (closed)";
        }
        return res;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void sendMessage(String content, Boolean crypted) throws IOException, AssertionError, NoSuchAlgorithmException, NoSuchPaddingException {
        Message msg = new Message(content, Origin.SERVER, crypted, MessageType.MESSAGE);
        this.messages.add(msg);
        if(crypted) {
            CryptedMessage cMsg = new CryptedMessage(content, Origin.SERVER, true, MessageType.MESSAGE);
            cMsg.encrypt(this.publicKey);
            msg = cMsg;
        }
        sendMessage(msg);
    }
    public void sendMessage(Message msg) {
        try {
            PrintWriter output = new PrintWriter(this.socket.getOutputStream(), true);
            assert msg != null;
            output.println(msg.getJSON());
        } catch (IOException | AssertionError e){
            e.printStackTrace();
            System.out.println("IO Exception: "+ e.toString());
        }
    }

    public Message listenForMessage() {
        if(this.socket.isClosed()) {
            return null;
        }
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            Message msg;
            String line;
            ObjectMapper mapper = new ObjectMapper();
            while (this.socket.isConnected() && (line = in.readLine()) != null) {
                System.out.println("New line: " + line);
                msg = mapper.readValue(line, Message.class);
                return msg;
            }
        } catch (java.net.SocketException e) {
            if(socket.isClosed()) {
                return null;
            }
            e.printStackTrace();
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Socket getSocket() { return this.socket; }

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
