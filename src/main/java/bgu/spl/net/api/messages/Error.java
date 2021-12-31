package bgu.spl.net.api.messages;

public class Error extends Message {
    private final short msgOpcode, myOpcode = 11;
    private String errorMsg;

    public Error(short msgOpcode) {
        super(Type.ERROR);
        this.msgOpcode = msgOpcode;
        errorMsg = "";
    }

    public Error(short msgOpcode, String errorMsg) {
        super(Type.ERROR);
        this.msgOpcode = msgOpcode;
        this.errorMsg = errorMsg;
    }

    @Override
    public byte[] toBytes() {
        byte[] myOp = shortToBytes(myOpcode);
        byte[] msgOp = shortToBytes(msgOpcode);
        byte[] errorMsg = this.errorMsg.getBytes();
        byte[] result = new byte[5 + errorMsg.length];
        System.arraycopy(myOp, 0, result, 0, 2);
        System.arraycopy(msgOp, 0, result, 2, 2);
        System.arraycopy(errorMsg, 0, result, 4, errorMsg.length);
        result[result.length - 1] = ';';
        return result;
    }
}
