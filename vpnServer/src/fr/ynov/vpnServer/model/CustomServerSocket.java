package fr.ynov.vpnServer.model;

import fr.ynov.vpnModel.model.Message;
import fr.ynov.vpnModel.model.CryptedMessage;
import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.MessageType;
import fr.ynov.vpnModel.model.SocketConfiguration;
import fr.ynov.vpnModel.model.Origin;
import fr.ynov.vpnModel.model.EncryptDecryptInterface;


import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CustomServerSocket extends ServerSocket implements EncryptDecryptInterface,EventInterface {

    private SecretKey privateKey;
    private SecretKey publicKey;
    private List<Socket> clients;

    private BiConsumer<CustomSocket, Message> onMessage;
    private Function<CustomSocket, Void> onConnect;
    private Function<CustomSocket, Void> onDisconnect;
    private Function<CustomSocket, Void> onError;


    // This is the constructor
    public CustomServerSocket(int port) throws Exception {
        super(port);
        new Thread(this::handleConnection).start();
    }

    public void setPrivateKey(SecretKey privateKey) {
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
    }

    private void handleConnection() {
        while(this.isBound()) {
            try {
                Socket s = this.accept();
                CustomSocket socket = new CustomSocket(s);
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
        while (socket.getSocket().isConnected()) {
            try {
                Message msg = socket.listenForMessage();
                if (msg != null) {
                    if(msg.getEvent() == MessageType.CONFIG) {
                        ConfigurationMessage confMessage = (ConfigurationMessage) msg;
                        SecretKey clientPubKey = new SecretKeySpec(Base64.getDecoder().decode(confMessage.getContent()), "AES");
                        socket.setPublicKey(clientPubKey);
                        this.sendServerKey(socket);
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

                e.printStackTrace();
            }
        }
        if(socket.getSocket().isClosed()) {
            onDisconnect(socket);
        }
    }

    public void sendMessage(String content, Boolean crypted, CustomSocket socket)
            throws IOException, AssertionError, NoSuchAlgorithmException {
        Message msg = null;
        if(crypted) {
            try {
                CryptedMessage cMsg = new CryptedMessage(content, Origin.SERVER, crypted, MessageType.MESSAGE);
                cMsg.encrypt(this.publicKey);
                msg = cMsg;
            } catch (Exception e) {
                System.out.println("IO Exception: "+ e.toString());
                sendMessage(content, false, socket);
            }
        }else {
            msg = new Message(content, Origin.SERVER, crypted, MessageType.MESSAGE);
        }
        assert msg != null;
        sendMessage(msg, socket);
    }
    public void sendMessage(Message msg, CustomSocket socket) throws IOException, AssertionError {
        try {
            OutputStream output = socket.getSocket().getOutputStream();
            assert msg != null;
            output.write(msg.getJSON().getBytes());
        } catch (IOException | AssertionError e){
            e.printStackTrace();
            System.out.println("IO Exception: "+ e.toString());
        }
    }

    @Override
    public void onMessage(CustomSocket cs, Message message) {
        if(onMessage != null) {
            this.onMessage.accept(cs, message);
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

    @Override
    public void setOnMessage(BiConsumer<CustomSocket, Message> onMessage) {
        this.onMessage = onMessage;
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
