package fr.ynov.vpnModel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class ConfigurationMessage extends Message implements EncryptDecryptInterface{

    private SocketConfiguration configuration;

    public ConfigurationMessage() {}

    public ConfigurationMessage(String content, Origin origin, boolean crypted, MessageType event, SocketConfiguration configuration) {
        super(content, origin, crypted,event);
        this.configuration = configuration;
    }
    @Override
    @JsonIgnore
    public String getJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public SocketConfiguration getConfiguration() {
        return configuration;
    }

    public String decrypt(SecretKey privateKey) throws Exception {
        if(configuration == SocketConfiguration.SEND_PUBLIC_KEY) {
            String newContent = this.decrypt(privateKey, this.getContent());
            this.setContent(newContent);
            this.crypted = false;
            return newContent;
        }
        return null;
    }
    public String encrypt(SecretKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException {
        String newContent = this.encrypt(privateKey, this.getContent());
        this.setContent(newContent);
        this.crypted = true;
        return newContent;
    }
}
