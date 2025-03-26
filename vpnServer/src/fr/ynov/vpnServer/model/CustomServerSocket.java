package fr.ynov.vpnServer.model;

import fr.ynov.vpnModel.model.EncryptDecryptInterface;
import fr.ynov.vpnModel.model.Message;
import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.CryptedMessage;
import fr.ynov.vpnModel.model.MessageType;
import fr.ynov.vpnModel.model.Origin;
import fr.ynov.vpnModel.model.SocketConfiguration;


import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;


public class CustomServerSocket extends ServerSocket implements EncryptDecryptInterface, EventInterface {

    private SecretKey privateKey;
    private SecretKey publicKey;
    private List<CustomSocket> clients = new ArrayList<>();

    private BiConsumer<CustomSocket, Message> onMessage;
    private BiConsumer<CustomSocket, ConfigurationMessage> onMessageConfiguration;
    private Function<CustomSocket, Void> onConnect;
    private Function<CustomSocket, Void> onDisconnect;
    private Function<CustomSocket, Void> onError;

    private String serverName;

    // This is the constructor
    public CustomServerSocket(int port, String name) throws Exception {
        super(port);
        this.serverName = name;
        new Thread(this::handleConnection).start();
    }

    public void setPrivateKey(SecretKey privateKey) {
        this.publicKey = privateKey;
        this.privateKey = privateKey;
    }
    // This method is used to reply to the 'GET_PUBLIC_KEY' from clientSocket
    public void sendServerKey(CustomSocket socket) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException {
        String pubKey = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
        pubKey = this.encrypt(socket.getPublicKey(), pubKey);
        ConfigurationMessage confMessage = new ConfigurationMessage(
                pubKey, Origin.SERVER, true, MessageType.CONFIG,
                SocketConfiguration.SEND_PUBLIC_KEY);
        socket.sendMessage(confMessage);
        socket.addMessage(confMessage);
    }

    public void sendName(CustomSocket socket) throws IOException {
        try {
            ConfigurationMessage confMessage = new ConfigurationMessage(
                    serverName, Origin.SERVER, true, MessageType.CONFIG,
                    SocketConfiguration.SET_NAME);
            socket.addMessage(confMessage);
            confMessage.encrypt(socket.getPublicKey());
            socket.sendMessage(confMessage);


        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {}
    }

    private void handleConnection() {
        while(this.isBound()) {
            try {
                Socket s = this.accept();
                CustomSocket socket = new CustomSocket(s);
                clients.add(socket);
                onConnect(socket);
                new Thread(() -> {
                    try {
                        this.handleMessage(socket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchPaddingException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ;
        }
    }

    private void handleMessage(CustomSocket socket) throws Exception {
        while (socket != null && socket.getSocket() != null && socket.getSocket().isConnected()) {
            try {
                Message msg = socket.listenForMessage();
                if (msg != null) {
                    if(msg.getType() == MessageType.CONFIG) {
                        ConfigurationMessage confMessage = (ConfigurationMessage) msg;
                        parseConfigFromMessage(socket, confMessage);
                        continue;
                    }
                    if(msg.isCrypted()) {
                        CryptedMessage cryptedMessage = (CryptedMessage) msg;
                        cryptedMessage.decrypt(this.privateKey);
                        msg = cryptedMessage;
                    }
                    socket.addMessage(msg);
                }
                onMessage(socket, msg);
            }catch (Exception e) {

            }
        }
        if(socket.getSocket().isClosed()) {
            onDisconnect(socket);
        }
    }

    private void parseConfigFromMessage(CustomSocket socket, ConfigurationMessage confMessage) {
        try {
            if(confMessage.getOrigin() != Origin.CLIENT) {return;}
            if(confMessage.isCrypted()) {
               confMessage.decrypt(this.privateKey);
            }
            switch (confMessage.getConfiguration()) {
                case GET_PUBLIC_KEY ->{
                    SecretKey clientPubKey = new SecretKeySpec(Base64.getDecoder().decode(confMessage.getContent()), "AES");
                    socket.setPublicKey(clientPubKey);
                    this.sendServerKey(socket);
                    break;
                }
                case SET_NAME -> {
                    socket.setName(confMessage.getContent());
                    sendName(socket);
                    break;
                }
                default -> {
                    break;
                }

            }
            socket.addMessage(confMessage);
            onMessageConfiguration(socket, confMessage);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // Event listener
    @Override
    public void onMessage(CustomSocket cs, Message message) {
        if(onMessage != null) {
            this.onMessage.accept(cs, message);
        }
    }
    @Override
    public void onMessageConfiguration(CustomSocket cs, ConfigurationMessage message) {
        if(onMessageConfiguration != null) {
            this.onMessageConfiguration.accept(cs, message);
        }
    }
    @Override
    public void onConnect(CustomSocket socket) {
        if(onConnect != null) {
            this.onConnect.apply(socket);
        }
    }
    @Override
    public void onDisconnect(CustomSocket socket) {
        if(onDisconnect != null) {
            this.onDisconnect.apply(socket);
        }
    }
    @Override
    public void onError(CustomSocket socket) {
        if(onError != null) {
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
