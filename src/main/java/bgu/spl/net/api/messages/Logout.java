package bgu.spl.net.api.messages;


public class Logout extends Message {
    public Logout(int connId) {
        super(Type.LOGOUT, connId);
    }
}
