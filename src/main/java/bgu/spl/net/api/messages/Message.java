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

    public Message.Type getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public byte[] toBytes() {
        return null;
    }

    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }
}
