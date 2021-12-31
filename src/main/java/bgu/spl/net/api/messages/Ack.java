package bgu.spl.net.api.messages;

public class Ack extends Message {
    private final short msgOpcode, myOpcode = 10;
    private int followOpcode;
    private String username;
    private int age, numPosts, numFollowers, numFollowing;

    public Ack(short msgOpcode) {
        super(Type.ACK);
        this.msgOpcode = msgOpcode;
    }

    public Ack(short msgOpcode, int followOpcode, String username) { // FOLLOW constructor
        super(Type.ACK);
        this.msgOpcode = msgOpcode;
        this.followOpcode = followOpcode;
        this.username = username;
    }

    public Ack(short msgOpcode, int age, int numPosts, int numFollowers, int numFollowing) {
        super(Type.ACK);
        this.msgOpcode = msgOpcode;
        this.age = age;
        this.numPosts = numPosts;
        this.numFollowers = numFollowers;
        this.numFollowing = numFollowing;
    }

    @Override
    public byte[] toBytes() {
        byte[] result = new byte[5];
        byte[] myOp = shortToBytes(myOpcode);
        byte[] msgOp = shortToBytes(msgOpcode);
        System.arraycopy(myOp, 0, result, 0, 2);
        System.arraycopy(msgOp, 0, result, 2, 2);
        result[4] = ';';
        return result;
    }
}
