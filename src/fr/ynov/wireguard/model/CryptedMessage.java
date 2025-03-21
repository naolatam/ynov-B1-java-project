package fr.ynov.wireguard.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.*;
import java.security.NoSuchAlgorithmException;


public class CryptedMessage extends Message implements EncryptDecryptInterface {


    public CryptedMessage(String content, Origin origin, boolean crypted, MessageType event) {
        super(content, origin, crypted, event);
    }
    @Override
    public String getJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public String decrypt(SecretKey privateKey) throws Exception {
            String newContent = this.decrypt(privateKey, this.getContent());
            this.setContent(newContent);
            this.crypted = false;
            return newContent;
    }

    public String encrypt(SecretKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException {
        String newContent = this.encrypt(privateKey, this.getContent());
        this.setContent(newContent);
        this.crypted = true;
        return newContent;
    }
}
