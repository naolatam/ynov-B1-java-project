package fr.ynov.wireguard.model;

import java.net.Socket;

public class Message {
        private MessageEvent event;
        private String content;
        private Socket origin;
        public boolean crypted;

        public Message(String content, Socket origin, boolean crypted, MessageEvent event) {
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
        public Socket getOrigin() {
            return this.origin;
        }
        public MessageEvent getEvent() {
            return this.event;
        }

}
