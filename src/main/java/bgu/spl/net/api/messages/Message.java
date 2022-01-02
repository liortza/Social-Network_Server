package bgu.spl.net.api.messages;

public abstract class Message {

    public enum Type {
        PLACEHOLDER, REGISTER, LOGIN, LOGOUT, FOLLOW, POST, PM, LOGSTAT, STAT, NOTIFICATION, ACK, ERROR, BLOCK
    }

    protected Type type;
    protected int connId;

    public Message(Type type, int connID) {
        this.type = type;
        this.connId = connID;
    }

    public Message(Type type) {
        this.type = type;
    }

    public Message.Type getType() {
        return type;
    }

    public int getConnId() {
        return connId;
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
