package fr.ynov.vpnModel.model;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.*;
import java.security.NoSuchAlgorithmException;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class CryptedMessage extends Message implements EncryptDecryptInterface {

    public CryptedMessage() {}

    public CryptedMessage(String content, Origin origin, boolean crypted, MessageType event) {
        super(content, origin, crypted, event);
    }
    @Override
    @JsonIgnore
    public String getJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public String decrypt(SecretKey privateKey) {
            if(isCrypted()) {
                String newContent = this.decrypt(privateKey, this.getContent());
                this.setContent(newContent);
                this.crypted = false;
                return newContent;
            }
            return null;
    }

    public String encrypt(SecretKey privateKey)  {
        String newContent = this.encrypt(privateKey, this.getContent());
        this.setContent(newContent);
        this.crypted = true;
        return newContent;
    }
}
