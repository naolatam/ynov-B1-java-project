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

public class serverSocket extends ServerSocket implements EncryptDecryptInterface {

    private SecretKey privateKey;
    private SecretKey publicKey;
    private List<Socket> clients;

    // This is the constructor
    public serverSocket(int port) throws Exception {
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
            try (CustomSocket socket = (CustomSocket) this.accept()) {
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
            } ;
        }
    }

    private void handleMessage(CustomSocket socket) throws Exception {
        while (socket.isConnected()) {
            try {
                Message msg = socket.listenForMessage();
                if (msg != null) {
                    if(msg.getType() == MessageType.CONFIG) {
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
            }catch (Exception e) {
                e.printStackTrace();
            }
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
            OutputStream output = socket.getOutputStream();
            assert msg != null;
            output.write(msg.getJSON().getBytes());
        } catch (IOException | AssertionError e){
            e.printStackTrace();
            System.out.println("IO Exception: "+ e.toString());
        }
    }

}
