package fr.ynov.vpnServer.model;

import fr.ynov.vpnModel.model.Message;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface EventInterface {

    public void onMessage(CustomSocket socket, Message message);
    public void onConnect(CustomSocket socket);
    public void onDisconnect(CustomSocket socket);
    public void onError(CustomSocket socket);

    public void setOnMessage(BiConsumer<CustomSocket, Message> onMessage);
    public void setOnConnect(Function<CustomSocket, Void> onConnect);
    public void setOnDisconnect(Function<CustomSocket, Void> onDisconnect);
    public void setOnError(Function<CustomSocket, Void> onError);


}
