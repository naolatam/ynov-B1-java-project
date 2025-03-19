package fr.ynov.wireguard.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        ConfigurationMessage confMessage = new ConfigurationMessage(pubKey, true, MessageType.CONFIG, SocketConfiguration.SEND_PUBLIC_KEY);
        socket.sendMessage(confMessage);
    }

    private void handleConnection() {
        while(true) {
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
                    }
                }).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } ;
        }
    }

    private void handleMessage(CustomSocket socket) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException {

    }

    public void sendMessage(String content, Boolean crypted) throws IOException, AssertionError, NoSuchAlgorithmException {
        Message msg = null;
        if(crypted) {
            try {

                CryptedMessage cMsg = new CryptedMessage(content, crypted, MessageType.MESSAGE);
                cMsg.encrypt(this.serverKey);
                msg = cMsg;
            } catch (Exception e) {
                System.out.println("IO Exception: "+ e.toString());
                sendMessage(content, false);
            }
        }else {
            msg = new Message(content, this, crypted, MessageType.MESSAGE);
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
