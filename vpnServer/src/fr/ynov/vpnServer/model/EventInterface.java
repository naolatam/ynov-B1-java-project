package fr.ynov.vpnServer.model;

import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.Message;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface EventInterface {

    // Message event
    public void onMessage(CustomSocket socket, Message message);
    public void onMessageConfiguration(CustomSocket socket, ConfigurationMessage message);

    public void onConnect(CustomSocket socket);
    public void onDisconnect(CustomSocket socket);
    public void onError(CustomSocket socket);

    // Message event setter
    public void setOnMessage(BiConsumer<CustomSocket, Message> onMessage);
    public void setOnMessageConfiguration(BiConsumer<CustomSocket, ConfigurationMessage> onMessageConfiguration);

    public void setOnConnect(Function<CustomSocket, Void> onConnect);
    public void setOnDisconnect(Function<CustomSocket, Void> onDisconnect);
    public void setOnError(Function<CustomSocket, Void> onError);


}
