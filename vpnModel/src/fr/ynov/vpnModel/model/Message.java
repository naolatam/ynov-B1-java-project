package fr.ynov.vpnModel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInput;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class Message {
        private MessageType event;
        private String content;
        private Origin origin;
        protected boolean crypted;

    public Message() {
    }


    public Message(String content, Origin origin, boolean crypted, MessageType event) {
            this.content = content;
            this.origin = origin;
            this.crypted = crypted;
            this.event = event;
        }
        public String getContent() {
            return this.content;
        }
        public void setContent(String content) {
            this.content = content;
        }
        public Origin getOrigin() {
            return this.origin;
        }
        
        @JsonIgnore
        public String getJSON() throws JsonProcessingException {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        }
    public boolean getCrypted() {
            return this.crypted;
    }
    public MessageType getEvent() {
            return this.event;
        }
        public Boolean isCrypted() {
            return this.crypted;
        }




}
