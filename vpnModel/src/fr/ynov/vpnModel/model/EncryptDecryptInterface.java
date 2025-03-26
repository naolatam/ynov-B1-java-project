package fr.ynov.vpnModel.model;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public interface EncryptDecryptInterface {
    public default String decrypt(SecretKey privateKey, String content){
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(content);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println("Unable to decrypt content : " + e.getMessage());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.out.println("Unable to decrypt content due to invalid algo or PaddingException: " + e.getMessage());
        }
        return null;
    }
    public default String encrypt(SecretKey privateKey, String content) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] crypted = cipher.doFinal(content.getBytes());
            return Base64.getEncoder().encodeToString(crypted);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println("Unable to decrypt content : " + e.getMessage());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.out.println("Unable to decrypt content due to invalid algo or PaddingException: " + e.getMessage());
        }
        return null;
    }

}
