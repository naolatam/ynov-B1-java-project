package fr.ynov.vpnClient.model;

import com.fasterxml.jackson.databind.ObjectMapper;


import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

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
    private List<Message> messages = new ArrayList<>();

    private UUID uuid = UUID.randomUUID();
    private String name;
    private String serverName;


    public ClientSocket(String host, int port, String name) throws Exception {
        connect(new InetSocketAddress(host, port), 5000);
        this.name = name;
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
    public void sendName() {
        try {
            ConfigurationMessage confMessage = new ConfigurationMessage(name, Origin.CLIENT, false, MessageType.CONFIG, SocketConfiguration.SET_NAME);
            confMessage.encrypt(this.serverKey);
            sendMessage(confMessage);
        } catch (IOException ex) {
            try {this.close();} catch (IOException ex1) {}
            System.out.println(ex.getMessage());

        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public String getServerName() {
        return this.serverName;
    }

    public List<Message> getMessages() {
        return this.messages;
    }

    private void listenMessage() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getInputStream()));
            Message msg;
            String line;
            ObjectMapper mapper = new ObjectMapper();

            while (this.isConnected() && (line = in.readLine()) != null) {
                System.out.println("New line: " +line);
                msg = mapper.readValue(line, Message.class);

                if(msg.getType() == MessageType.CONFIG) {
                    parseConfigFromMessage((ConfigurationMessage) msg);
                    continue;
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
                    }
                }
                this.messages.add(msg);
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

    private void parseConfigFromMessage(ConfigurationMessage confMessage) {
        try {
            if(confMessage.isCrypted()) {
                confMessage.decrypt(this.privateKey);
            }
            if(confMessage.getContent() == null) {
                askServerKey();
                return;
            }
            if(confMessage.getConfiguration() == SocketConfiguration.SEND_PUBLIC_KEY) {
                this.serverKey= new SecretKeySpec(Base64.getDecoder().decode(confMessage.getContent()), "AES");
                this.messages.add(confMessage);
                sendName();
            }
            if(confMessage.getConfiguration() == SocketConfiguration.SET_NAME) {
                this.serverName = confMessage.getContent();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        if(serverName == null) {
            return uuid.toString();
        }
        return serverName;
    }

}