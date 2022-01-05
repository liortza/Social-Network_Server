package bgu.spl.net.api.messages;

public class PM extends Message {
    private final String content, recipient, dateTime;

    public PM(int connId, String recipient, String content, String dateTime) {
        super(Type.PM, connId);
        this.content = content;
        this.recipient = recipient;
        this.dateTime = dateTime;
    }

    public String getContent() { return content; }

    public String getRecipient() { return recipient; }
}
