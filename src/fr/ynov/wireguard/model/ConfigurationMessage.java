package fr.ynov.wireguard.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class ConfigurationMessage extends Message implements EncryptDecryptInterface{

    private final SocketConfiguration configuration;

    public ConfigurationMessage(String content, boolean crypted, MessageType event, SocketConfiguration configuration) {
        super(content, null, crypted,event);
        this.configuration = configuration;
    }
    @Override
    public String getJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
    @Override
    public String decrypt(SecretKey privateKey) throws Exception {
        if(configuration == SocketConfiguration.SEND_PUBLIC_KEY) {
            String newContent = this.decrypt(privateKey, this.getContent());
            this.setContent(newContent);
            this.crypted = false;
            return newContent;
        }
        return null;
    }
}
