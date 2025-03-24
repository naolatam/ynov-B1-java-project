package fr.ynov.vpnServer.model;

import fr.ynov.vpnModel.model.Message;

import java.util.function.Function;

public interface EventInterface {

    public void onMessage(Message message);
    public void onConnect(CustomSocket socket);
    public void onDisconnect(CustomSocket socket);
    public void onError(CustomSocket socket);

    public void setOnMessage(Function<Message, Void> onMessage);
    public void setOnConnect(Function<CustomSocket, Void> onConnect);
    public void setOnDisconnect(Function<CustomSocket, Void> onDisconnect);
    public void setOnError(Function<CustomSocket, Void> onError);


}
