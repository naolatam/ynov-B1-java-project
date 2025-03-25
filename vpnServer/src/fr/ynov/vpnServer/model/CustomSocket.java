package fr.ynov.vpnServer.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import fr.ynov.vpnModel.model.Message;
import fr.ynov.vpnModel.model.CryptedMessage;
import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.MessageType;
import fr.ynov.vpnModel.model.SocketConfiguration;
import fr.ynov.vpnModel.model.Origin;
import fr.ynov.vpnModel.model.EncryptDecryptInterface;


public class CustomSocket extends Socket {

    private SecretKey publicKey;
    private SecretKey serverKey;

    private List<Message> messages;


    public CustomSocket(SecretKey serverKey) {
        this.serverKey = serverKey;
    }

    public void setPublicKey(SecretKey publicKey) {
        this.publicKey = publicKey;
    }

    public SecretKey getPublicKey() {
        return this.publicKey;
    }

    public void sendMessage(String content, Boolean crypted) throws IOException, AssertionError, NoSuchAlgorithmException {
        Message msg = new Message(content, Origin.SERVER, crypted, MessageType.MESSAGE);
        sendMessage(msg);
    }
    public void sendMessage(Message msg) throws IOException, AssertionError {
        try {
            OutputStream output = this.getOutputStream();
            assert msg != null;
            output.write(msg.getJSON().getBytes());
            this.messages.add(msg);
        } catch (IOException | AssertionError e){
            e.printStackTrace();
            System.out.println("IO Exception: "+ e.toString());
        }
    }

    public Message listenForMessage() throws IOException, NoSuchPaddingException, NoSuchAlgorithmException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getInputStream()));
            Message msg;
            ConfigurationMessage confMessage;
            String line;
            ObjectMapper mapper = new ObjectMapper();
<<<<<<< Updated upstream
            while (this.isConnected() && (line = in.readLine()) != null) {
                System.out.println("New line: " + line);
=======
            while (this.socket.isConnected() && (line = in.readLine()) != null) {
>>>>>>> Stashed changes
                msg = mapper.readValue(line, Message.class);
                return msg;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Exception: " + e.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }



    public List<Message> getMessages() {
        return this.messages;
    }
    public void addMessage(Message msg) {
        this.messages.add(msg);
    }
}
