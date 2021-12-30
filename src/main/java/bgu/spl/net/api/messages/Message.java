package bgu.spl.net.api.messages;

public abstract class Message {

    public enum Type {
        PLACEHOLDER, REGISTER, LOGIN, LOGOUT, FOLLOW, POST, PM, LOGSTAT, STAT, NOTIFICATION, ACK, ERROR, BLOCK
    }

    protected Type type;
    protected int id;

    public Message(Type type, int id) {
        this.type = type;
        this.id = id;
    }

    public Message(Type type) {
        this.type = type;
    }

    public int getId() { return id; }
}
