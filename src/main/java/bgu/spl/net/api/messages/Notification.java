package bgu.spl.net.api.messages;

public class Notification extends Message {

    private String type;
    private byte notificationType;
    private String postingUser, content;
    private short myOpcode = 9;

    public Notification(Message.Type type, String postingUser, String content) {
        super(Type.NOTIFICATION);
        if (type == Type.PM) {
            this.type = "PM";
            notificationType = 0;
        } else { // post
            this.type ="PUBLIC";
            notificationType = 1;
        }
        this.postingUser = postingUser;
        this.content = content;
    }

    @Override
    public byte[] toBytes() {
        int index;
        byte[] postingUser = this.postingUser.getBytes();
        byte[] content = this.content.getBytes();
        byte[] myOp = shortToBytes(myOpcode);
        byte[] result = new byte[6 + postingUser.length + content.length];
        System.arraycopy(myOp, 0, result, 0, 2);
        result[2] = notificationType;
        System.arraycopy(postingUser, 0, result, 3, postingUser.length);
        index = 2 + postingUser.length;
        result[index] = '\0';
        System.arraycopy(content, 0, result, index + 1, content.length);
        result[result.length - 2] = '\0';
        result[result.length - 1] = ';';
        return result;
    }
}
