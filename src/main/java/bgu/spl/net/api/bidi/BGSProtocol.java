package bgu.spl.net.api.bidi;

import bgu.spl.net.api.messages.Message;

public class BGSProtocol<T extends Message> implements BidiMessagingProtocol<T>{
    private boolean shouldTerminate = false;
    private int connectionId;
    private Connections<T> connections;

    public void start(int connectionId, Connections<T> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        // connections.add(handler);
    }

    public void process(T msg) {
        // handleRegister -- if true -> start()
    }

    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
