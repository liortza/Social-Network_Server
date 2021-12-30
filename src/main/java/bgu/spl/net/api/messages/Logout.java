package bgu.spl.net.api.messages;


public class Logout extends Message {
    public Logout(int id) {
        super(Type.LOGOUT, id);
    }
}
