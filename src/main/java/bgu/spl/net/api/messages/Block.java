package bgu.spl.net.api.messages;

public class Block extends Message {
    private final String toBlock;

    public Block(int connId, String toBlock) {
        super(Type.BLOCK, connId);
        this.toBlock = toBlock;
    }

    public String getToBlock() {
        return toBlock;
    }
}
