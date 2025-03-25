package fr.ynov.vpnModel.model;


<<<<<<< Updated upstream
=======
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
>>>>>>> Stashed changes
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class ConfigurationMessage extends Message implements EncryptDecryptInterface{

    private SocketConfiguration configuration;

    public ConfigurationMessage() {}

    public ConfigurationMessage(String content, Origin origin, boolean crypted, MessageType event, SocketConfiguration configuration) {
        super(content, origin, crypted,event);
        this.configuration = configuration;
    }
    @Override
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
}
