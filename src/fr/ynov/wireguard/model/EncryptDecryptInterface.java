package fr.ynov.wireguard.model;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public interface EncryptDecryptInterface {
    public default String decrypt(SecretKey privateKey, String content) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(content);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
                        return decryptedBytes.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public default String encrypt(SecretKey privateKey, String content) throws NoSuchPaddingException, NoSuchAlgorithmException {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] contentBytes = content.getBytes();
            byte[] crypted = cipher.doFinal(contentBytes);
            return crypted.toString();
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

}
