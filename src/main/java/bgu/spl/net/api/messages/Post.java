package bgu.spl.net.api.messages;

import java.util.LinkedList;

public class Post extends Message {
    private final String content;
    private final LinkedList<String> taggedUsers;

    public Post(int id, String content, LinkedList<String> taggedUsers) {
        super(Type.POST, id);
        this.content = content;
        this.taggedUsers = taggedUsers;
    }

    public String getContent() { return content; }

    public LinkedList<String> getTaggedUsers() { return taggedUsers; }
}
