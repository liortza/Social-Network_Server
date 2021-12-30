package bgu.spl.net.api.messages;

public class PM extends Message {
    private final String content, recipient, dateTime;

    public PM(int id, String content, String recipient, String dateTime) {
        super(Type.PM, id);
        this.content = content;
        this.recipient = recipient;
        this.dateTime = dateTime;
    }

    public String getContent() { return content; }

    public String getRecipient() { return recipient; }
}
