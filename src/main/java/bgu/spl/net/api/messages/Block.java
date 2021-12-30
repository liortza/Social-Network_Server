package bgu.spl.net.api.messages;

public class Block extends Message {
    private final String toBlock;

    public Block(int id, String toBlock) {
        super(Type.BLOCK, id);
        this.toBlock = toBlock;
    }

    public String getToBlock() {
        return toBlock;
    }
}
