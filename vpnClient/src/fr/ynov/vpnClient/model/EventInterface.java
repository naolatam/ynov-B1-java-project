package fr.ynov.vpnClient.model;

import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.Message;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface EventInterface {

    // Message event
    public void onMessage(ClientSocket socket, Message message);
    public void onMessageConfiguration(ClientSocket socket, ConfigurationMessage message);

    // Event listener
    public void onConnect(ClientSocket socket);
    public void onDisconnect(ClientSocket socket);
    public void onError(ClientSocket socket);

    // Message event setter
    public void setOnMessage(BiConsumer<ClientSocket, Message> onMessage);
    public void setOnMessageConfiguration(BiConsumer<ClientSocket, ConfigurationMessage> onMessageConfiguration);

    // Event listener setter
    public void setOnConnect(Function<ClientSocket, Void> onConnect);
    public void setOnDisconnect(Function<ClientSocket, Void> onDisconnect);
    public void setOnError(Function<ClientSocket, Void> onError);


}
