package fr.ynov.vpnClient.model;

import com.fasterxml.jackson.databind.ObjectMapper;


import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import fr.ynov.vpnModel.model.Message;
import fr.ynov.vpnModel.model.CryptedMessage;
import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.MessageType;
import fr.ynov.vpnModel.model.SocketConfiguration;
import fr.ynov.vpnModel.model.Origin;



public class ClientSocket extends Socket {

    private SecretKey privateKey;
    private SecretKey publicKey;
    private SecretKey serverKey;
    private List<Message> messages;


    public ClientSocket(String host, int port) throws Exception {
        connect(new InetSocketAddress(host, port), 5000);
        new Thread(this::listenMessage).start();
    }

    public void setPrivateKey(SecretKey privateKey) {
        this.publicKey = privateKey;
        this.privateKey = privateKey;
    }

    public void askServerKey() {
        try {
            String pubKey = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
            ConfigurationMessage confMessage = new ConfigurationMessage(pubKey, Origin.CLIENT, false, MessageType.CONFIG, SocketConfiguration.GET_PUBLIC_KEY);
            sendMessage(confMessage);
        } catch (IOException ex) {
            try {this.close();} catch (IOException ex1) {}
            System.out.println(ex.getMessage());

        }
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
            PrintWriter out = new PrintWriter(getOutputStream(), true);
            assert msg != null;
            out.println(msg.getJSON());
        } catch (IOException | AssertionError e){
            e.printStackTrace();
            System.out.println("IO Exception: "+ e.toString());
        }
    }

}