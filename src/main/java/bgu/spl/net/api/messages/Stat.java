package bgu.spl.net.api.messages;

import java.util.LinkedList;

public class Stat extends Message {
    private final LinkedList<String> usernames;

    public Stat(int connId, LinkedList<String> usernames) {
        super(Type.STAT, connId);
        this.usernames = usernames;
    }

    public LinkedList<String> getUsernames() { return usernames; }
}
