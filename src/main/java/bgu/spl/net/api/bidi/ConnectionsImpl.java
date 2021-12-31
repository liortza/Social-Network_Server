package bgu.spl.net.api.bidi;

import bgu.spl.net.api.messages.Message;
import bgu.spl.net.srv.ConnectionHandler;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T extends Message> implements Connections<T> {
    private final ConcurrentHashMap<Integer, ConnectionHandler<T>> connections = new ConcurrentHashMap<>();

    public boolean send(int connectionId, T msg) {
        if (connections.containsKey(connectionId)) {
            connections.get(connectionId).send(msg);
            return true;
        } return false;
    }

    public void broadcast(T msg) {
        for (ConnectionHandler<T> handler: connections.values())
            handler.send(msg);
    }

    public void connect(int connectionId, ConnectionHandler<T> handler) {
        if (!connections.containsKey(connectionId))
            connections.put(connectionId, handler);
    }

    public void disconnect(int connectionId) {
        connections.remove(connectionId);
    }

    public ConnectionHandler<T> getConnection(int connectionId) {
        return connections.get(connectionId);
    }
}
