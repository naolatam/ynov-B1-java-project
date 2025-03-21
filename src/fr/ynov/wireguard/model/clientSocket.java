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

public class clientSocket extends Socket {

    private SecretKey privateKey;
    private SecretKey publicKey;
    private SecretKey serverKey;
    private List<Message> messages;


    public clientSocket(String host, int port) throws Exception {
        super(host, port);
        new Thread(this::listenMessage).start();
    }

    public void setPrivateKey(SecretKey privateKey) {
        this.privateKey = privateKey;
    }

    public void askServerKey() throws IOException {
        String pubKey = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
        ConfigurationMessage confMessage = new ConfigurationMessage(pubKey, Origin.CLIENT, false, MessageType.CONFIG, SocketConfiguration.GET_PUBLIC_KEY);
        sendMessage(confMessage);
    }

    private void listenMessage() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getInputStream()));
            Message msg;
            ConfigurationMessage confMessage;
            String line;
            ObjectMapper mapper = new ObjectMapper();
            while (this.isConnected() && (line = in.readLine()) != null) {
                System.out.println("New line: " +line);
                msg = mapper.readValue(line, Message.class);

                if(msg.getEvent() == MessageType.CONFIG) {
                    confMessage = mapper.readValue(line, ConfigurationMessage.class);
                    confMessage.decrypt(this.privateKey);
                    if(confMessage.getContent() == null) {
                        askServerKey();
                        continue;
                    }
                    this.serverKey= new SecretKeySpec(Base64.getDecoder().decode(confMessage.getContent()), "AES");
                    this.messages.add(confMessage);
                }
                if(msg.isCrypted()){
                    if(this.privateKey == null) {
                        this.messages.add(msg);
                        continue;
                    }
                    CryptedMessage cMsg = (CryptedMessage) msg;
                    cMsg.decrypt(this.privateKey);
                    if(cMsg.getContent() == null) {
                        this.askServerKey();
                        this.messages.add(msg);
                        continue;
                    }
                    this.messages.add(cMsg);
                    continue;
                }
                this.messages.add(msg);
                continue;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Exception: "+ e.toString());
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String content, Boolean crypted) throws IOException, AssertionError, NoSuchAlgorithmException {
        Message msg = null;
        if(crypted) {
            try {

                CryptedMessage cMsg = new CryptedMessage(content, Origin.CLIENT, crypted, MessageType.MESSAGE);
                cMsg.encrypt(this.serverKey);
                msg = cMsg;
            } catch (Exception e) {
                System.out.println("IO Exception: "+ e.toString());
                sendMessage(content, false);
            }
        }else {
            msg = new Message(content, Origin.CLIENT, crypted, MessageType.MESSAGE);
        }
        sendMessage(msg);
    }
    public void sendMessage(Message msg) throws IOException, AssertionError {
        try {
            OutputStream output = this.getOutputStream();
            assert msg != null;
            output.write(msg.getJSON().getBytes());
        } catch (IOException | AssertionError e){
            e.printStackTrace();
            System.out.println("IO Exception: "+ e.toString());
        }
    }

}
