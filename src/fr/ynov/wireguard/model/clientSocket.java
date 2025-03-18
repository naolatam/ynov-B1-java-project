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
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
        ConfigurationMessage confMessage = new ConfigurationMessage(pubKey, false, MessageEvent.CONFIG, SocketConfiguration.GET_PUBLIC_KEY);
        try {
            OutputStream output = this.getOutputStream();
            output.write(confMessage.getJSON().getBytes());
        } catch (IOException e){
            e.printStackTrace();
            System.out.println("IO Exception: "+ e.toString());
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

                if(msg.getEvent() == MessageEvent.CONFIG) {
                    confMessage = mapper.readValue(line, ConfigurationMessage.class);
                    confMessage.decrypt(this.privateKey);
                    if(confMessage.getContent() == null) {
                        askServerKey();
                        continue;
                    }
                    this.serverKey= new SecretKeySpec(Base64.getDecoder().decode(confMessage.getContent()), "AES");
                    this.messages.add(confMessage);
                }
                if(msg.crypted == true){
                    if(this.serverKey == null) {
                        this.askServerKey();
                        this.messages.add(msg);
                        continue;
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Exception: "+ e.toString());
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


}
