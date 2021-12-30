package bgu.spl.net.api.messages;

public class Error extends Message {
    private final int opcode;
    private String errorMsg;

    public Error(int opcode) {
        super(Type.ERROR);
        this.opcode = opcode;
    }

    public Error(int opcode, String errorMsg) {
        super(Type.ERROR);
        this.opcode = opcode;
        this.errorMsg = errorMsg;
    }
}
