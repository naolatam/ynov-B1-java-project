package fr.ynov.wireguard.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

public class CustomSocket {

    private SecretKey publicKey;
    private Socket socket;
    private List<Message> messages;


    public CustomSocket(Socket s) {
        this.socket = s;
    }

    public void setPublicKey(SecretKey publicKey) {
        this.publicKey = publicKey;
    }

    public SecretKey getPublicKey() {
        return this.publicKey;
    }

    public void sendMessage(String content, Socket origin, Boolean crypted) throws IOException, AssertionError, NoSuchAlgorithmException {
        Message msg = new Message(content, origin, crypted, MessageType.MESSAGE);
        sendMessage(msg);
    }
    public void sendMessage(Message msg) throws IOException, AssertionError {
        try {
            OutputStream output = this.socket.getOutputStream();
            assert msg != null;
            output.write(msg.getJSON().getBytes());
            this.messages.add(msg);
        } catch (IOException | AssertionError e){
            e.printStackTrace();
            System.out.println("IO Exception: "+ e.toString());
        }
    }
    public List<Message> getMessages() {
        return this.messages;
    }
}
