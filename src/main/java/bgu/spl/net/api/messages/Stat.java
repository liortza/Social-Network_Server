package bgu.spl.net.api.messages;

import java.util.LinkedList;

public class Stat extends Message {
    private final LinkedList<String> usernames;

    public Stat(int id, LinkedList<String> usernames) {
        super(Type.STAT, id);
        this.usernames = usernames;
    }

    public LinkedList<String> getUsernames() { return usernames; }
}
