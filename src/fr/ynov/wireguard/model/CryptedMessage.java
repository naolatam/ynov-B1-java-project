package fr.ynov.wireguard.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class CryptedMessage extends Message {


    public CryptedMessage(String content, boolean crypted, MessageEvent event) {
        super(content, null, crypted, event);
    }
    public String getJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public String decrypt(SecretKey privateKey) throws {
            try {
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                byte[] encryptedBytes = Base64.getDecoder().decode(this.getContent());
                byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
                this.setContent(decryptedBytes.toString());
                return decryptedBytes.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
    }
    public String encrypt(SecretKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] contentBytes = this.getContent().getBytes();
            byte[] crypted = cipher.doFinal(contentBytes);
            this.setContent(crypted.toString());
            return crypted.toString();
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}
