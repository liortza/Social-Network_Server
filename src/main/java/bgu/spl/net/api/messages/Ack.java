package bgu.spl.net.api.messages;

public class Ack extends Message {
    private final short msgOpcode, myOpcode = 10;
    private String username;
    private short type, age, numPosts, numFollowers, numFollowing;

    public Ack(short msgOpcode) { // default constructor
        super(Type.ACK);
        this.msgOpcode = msgOpcode;
        type = 1;
    }

    public Ack(short msgOpcode, String username) { // FOLLOW constructor
        super(Type.ACK);
        this.msgOpcode = msgOpcode;
        this.username = username;
        type = 2;
    }

    public Ack(short msgOpcode, short age, short numPosts, short numFollowers, short numFollowing) { // STAT, LOGSTAT
        super(Type.ACK);
        this.msgOpcode = msgOpcode;
        this.age = age;
        this.numPosts = numPosts;
        this.numFollowers = numFollowers;
        this.numFollowing = numFollowing;
        type = 3;
    }

    @Override
    public byte[] toBytes() {
        switch (type) {
            case 1: return defaultToBytes();
            case 2: return followToBytes();
            default: return statToBytes();
        }
    }

    private void addOpcodes(byte[] arr) {
        byte[] myOp = shortToBytes(myOpcode);
        byte[] msgOp = shortToBytes(msgOpcode);
        System.arraycopy(myOp, 0, arr, 0, 2);
        System.arraycopy(msgOp, 0, arr, 2, 2);
    }

    private byte[] defaultToBytes() {
        byte[] result = new byte[5];
        addOpcodes(result);
        result[4] = ';';
        return result;
    }

    private byte[] followToBytes() {
        byte[] user = username.getBytes();
        byte[] result = new byte[5 + user.length];
        addOpcodes(result);
        System.arraycopy(user, 0, result, 4, user.length);
        result[result.length - 1] = ';';
        return result;
    }

    private byte[] statToBytes() {
        byte[] age = shortToBytes(this.age);
        byte[] numPosts = shortToBytes(this.numPosts);
        byte[] numFollowers = shortToBytes(this.numFollowers);
        byte[] numFollowing = shortToBytes(this.numFollowing);
        byte[] result = new byte[13];
        addOpcodes(result);
        System.arraycopy(age, 0, result, 4, 2);
        System.arraycopy(numPosts, 0, result, 6, 2);
        System.arraycopy(numFollowers, 0, result, 8, 2);
        System.arraycopy(numFollowing, 0, result, 10, 2);
        result[12] = ';';
        return result;
    }
}
