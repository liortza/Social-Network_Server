package bgu.spl.net.api.messages;

public class Notification extends Message {

    private String type;
    private String postingUser, content;

    public Notification(Message.Type type, String postingUser, String content) {
        super(Type.NOTIFICATION);
        if (type == Type.PM) this.type = "PM";
        else this.type ="PUBLIC";
        this.postingUser = postingUser;
        this.content = content;
    }
}
