package bgu.spl.net.api.bidi;

import bgu.spl.net.api.messages.Message;
import bgu.spl.net.srv.ConnectionHandler;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl implements Connections<Message> {
    private final ConcurrentHashMap<Integer, ConnectionHandler<Message>> connections = new ConcurrentHashMap<>();

    public boolean send(int connectionId, Message msg) {
        if (connections.containsKey(connectionId)) {
            connections.get(connectionId).send(msg);
            return true;
        } return false;
    }

    public void broadcast(Message msg) {
        for (ConnectionHandler<Message> handler: connections.values())
            handler.send(msg);
    }

    public void connect(int connectionId, ConnectionHandler<Message> handler) {
        if (!connections.containsKey(connectionId))
            connections.put(connectionId, handler);
    }

    public void disconnect(int connectionId) {
        connections.remove(connectionId);
    }

}
