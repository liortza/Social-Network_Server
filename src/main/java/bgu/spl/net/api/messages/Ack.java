package bgu.spl.net.api.messages;

public class Ack extends Message {
    private final int opcode;
    private int followOpcode;
    private String username;
    private int age, numPosts, numFollowers, numFollowing;

    public Ack(int opcode) {
        super(Type.ACK);
        this.opcode = opcode;
    }

    public Ack(int opcode, int followOpcode, String username) { // FOLLOW constructor
        super(Type.ACK);
        this.opcode = opcode;
        this.followOpcode = followOpcode;
        this.username = username;
    }

    public Ack(int opcode, int age, int numPosts, int numFollowers, int numFollowing) {
        super(Type.ACK);
        this.opcode = opcode;
        this.age = age;
        this.numPosts = numPosts;
        this.numFollowers = numFollowers;
        this.numFollowing = numFollowing;
    }
}
