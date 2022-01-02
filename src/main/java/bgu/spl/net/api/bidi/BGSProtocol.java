package bgu.spl.net.api.bidi;

import bgu.spl.net.api.messages.*;
import bgu.spl.net.srv.ConnectionHandler;

public class BGSProtocol implements BidiMessagingProtocol<Message>{
    private boolean shouldTerminate = false;
    private int connectionId;
    private Connections<Message> connections;
    private Control<Message> control;
    private ConnectionHandler<Message> handler;

    public BGSProtocol(Control<Message> control) {
        this.control = control;
    }

    public void start(int connectionId, Connections<Message> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        ((ConnectionsImpl) connections).connect(connectionId, handler);
    }

    public void process(Message msg) { // only client -> server messages
        switch (msg.getType()) {
            case REGISTER:
                control.handleRegister((Register) msg);
                break;
            case LOGIN:
                control.handleLogin((Login) msg);
                break;
            case LOGOUT:
                control.handleLogout((Logout) msg); // TODO: need to call disconnect?? we don't want to remove from map
                shouldTerminate = true;
                break;
            case FOLLOW:
                control.handleFollow((Follow) msg);
                break;
            case POST:
                control.handlePost((Post) msg);
                break;
            case PM:
                control.handlePM((PM) msg);
                break;
            case LOGSTAT:
                control.handleLogStat((LogStat) msg);
                break;
            case STAT:
                control.handleStat((Stat) msg);
                break;
            case BLOCK:
                control.handleBlock((Block) msg);
                break;
        }
    }

    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    public void setHandler(ConnectionHandler<Message> handler) {
        this.handler = handler;
    }
}
