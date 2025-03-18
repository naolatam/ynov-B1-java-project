package fr.ynov.wireguard.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class ConfigurationMessage extends Message {

    private final SocketConfiguration configuration;

    public ConfigurationMessage(String content, boolean crypted, MessageEvent event, SocketConfiguration configuration) {
        super(content, null, crypted,event);
        this.configuration = configuration;
    }
    public String getJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public String decrypt(SecretKey privateKey) throws JsonProcessingException, NoSuchPaddingException, NoSuchAlgorithmException {
        if(configuration == SocketConfiguration.SEND_PUBLIC_KEY) {
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
        return null;
    }
}
