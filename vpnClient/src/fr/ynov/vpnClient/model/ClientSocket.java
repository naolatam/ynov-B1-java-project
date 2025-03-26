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
import java.util.function.BiConsumer;
import java.util.function.Function;

import fr.ynov.vpnModel.model.Message;
import fr.ynov.vpnModel.model.CryptedMessage;
import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.MessageType;
import fr.ynov.vpnModel.model.SocketConfiguration;
import fr.ynov.vpnModel.model.Origin;



public class ClientSocket extends Socket implements EventInterface {

    private SecretKey privateKey;
    private SecretKey publicKey;
    private SecretKey serverKey;
    private List<Message> messages = new ArrayList<>();

    private BiConsumer<ClientSocket, Message> onMessage;
    private BiConsumer<ClientSocket, ConfigurationMessage> onMessageConfiguration;
    private Function<ClientSocket, Void> onConnect;
    private Function<ClientSocket, Void> onDisconnect;
    private Function<ClientSocket, Void> onError;

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
            this.messages.add(confMessage);
            sendMessage(confMessage);
        } catch (IOException ex) {
            try {this.close();} catch (IOException ex1) {}
            System.out.println(ex.getMessage());

        }
    }
    public void sendName() {
        try {
            ConfigurationMessage confMessage = new ConfigurationMessage(name, Origin.CLIENT, false, MessageType.CONFIG, SocketConfiguration.SET_NAME);
            this.messages.add(confMessage);
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
                onMessage(this, msg);
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

    public void sendMessage(String content, Boolean crypted) throws IOException, AssertionError, NoSuchAlgorithmException, NoSuchPaddingException {
        Message msg = new Message(content, Origin.CLIENT, crypted, MessageType.MESSAGE);
        this.messages.add(msg);
        if(crypted) {
            CryptedMessage cMsg = new CryptedMessage(content, Origin.CLIENT, true, MessageType.MESSAGE);
            cMsg.encrypt(this.serverKey);
            msg = cMsg;
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
            this.messages.add(confMessage);
            if(confMessage.getConfiguration() == SocketConfiguration.SEND_PUBLIC_KEY) {
                this.serverKey= new SecretKeySpec(Base64.getDecoder().decode(confMessage.getContent()), "AES");
                sendName();
            }
            if(confMessage.getConfiguration() == SocketConfiguration.SET_NAME) {
                this.serverName = confMessage.getContent();
            }
            onMessageConfiguration(this, confMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {

        String res = "";
        if(serverName == null) {
            res += uuid.toString();
        }else {
            res += serverName;
        }
        if(this.isClosed()) {
            res += " (closed)";
        }
        return res;
    }

    @Override
    public void onMessage(ClientSocket socket, Message message) {
        if(onMessage != null) {
            onMessage.accept(socket, message);
        }
    }

    @Override
    public void onMessageConfiguration(ClientSocket socket, ConfigurationMessage message) {
        if(onMessageConfiguration != null) {
            onMessageConfiguration.accept(socket, message);
        }
    }

    @Override
    public void onConnect(ClientSocket socket) {
        if(onConnect != null) {
            onConnect.apply(socket);
        }
    }

    @Override
    public void onDisconnect(ClientSocket socket) {
        if(onDisconnect != null) {
            onDisconnect.apply(socket);
        }
    }

    @Override
    public void onError(ClientSocket socket) {
        if(onError != null) {
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