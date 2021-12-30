package bgu.spl.net.api.bidi;

import bgu.spl.net.api.messages.*;
import bgu.spl.net.api.messages.Error;
import bgu.spl.net.srv.Client;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Control<T extends Message> {
    private final Connections<Message> connections;
    private final String[] filtered = new String[]{" BBZNOT ", " WEED "};
    private final ConcurrentHashMap<String, Integer> usernames = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Client> clients = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String> postsAndPm = new ConcurrentLinkedQueue<>();

    public Control(Connections<Message> connections) {
        this.connections = connections;
    }

    private boolean isRegistered(int id) {
        return clients.containsKey(id);
    }

    private boolean isLoggedIn(int id) {
        return isRegistered(id) && clients.get(id).getLoggedIn();
    }

    public boolean handleRegister(Register register) {
        int id = register.getId();
        if (isRegistered(id)) {
            connections.send(id, new Error(Message.Type.REGISTER.ordinal()));
            return false;
        }
        Client client = new Client(id, register.getUserName(), register.getPassword(), register.getAge());
        usernames.put(register.getUserName(), id);
        clients.put(id, client);
        connections.send(id, new Ack(Message.Type.REGISTER.ordinal()));
        return true;
    }

    public void handleLogin(Login login) {
        int id = login.getId();
        if (!isRegistered(id) || isLoggedIn(id) ||
                login.getCapcha() == 0 | !login.getPassword().equals(clients.get(id).getPassword())) {
            connections.send(id, new Error(Message.Type.LOGIN.ordinal()));
            return;
        }
        clients.get(id).logIn();
        connections.send(id, new Ack(Message.Type.LOGIN.ordinal()));
    }

    public void handleLogout(Logout logout) {
        int id = logout.getId();
        if (!isLoggedIn(id)) {
            connections.send(id, new Error(Message.Type.LOGOUT.ordinal()));
            return;
        }
        clients.get(id).logOut();
        connections.send(id, new Ack(Message.Type.LOGOUT.ordinal()));
    }

    public void handleFollow(Follow follow) {
        int id = follow.getId();
        int toFollowId = usernames.get(follow.getUserToFollow());
        Client me = clients.get(id);
        Client toFollow = clients.get(toFollowId);
        if (!isLoggedIn(id) | !isRegistered(toFollow.getId())) {
            connections.send(id, new Error(Message.Type.FOLLOW.ordinal()));
            return;
        }

        ConcurrentLinkedQueue<Integer> followers = me.getFollowers();
        if (follow.followAction()) { // case follow
            if (followers.contains(toFollow.getId()) | me.isBlocked(toFollowId) | toFollow.isBlocked(id)) // TODO: need to return error??
                connections.send(id, new Error(Message.Type.FOLLOW.ordinal()));
            else {
                me.incrementFollowing();
                toFollow.addFollower(id);
                connections.send(id, new Ack(Message.Type.FOLLOW.ordinal(), 0, follow.getUserToFollow()));
            }
        } else { // case unfollow
            if (!followers.contains(toFollow.getId())) connections.send(id, new Error(Message.Type.FOLLOW.ordinal()));
            else {
                me.decrementFollowing();
                toFollow.removeFollower(id);
                connections.send(id, new Ack(Message.Type.FOLLOW.ordinal(), 1, follow.getUserToFollow()));
            }
        }
    }

    public void handlePost(Post post) {
        int id = post.getId();
        Client me = clients.get(id);
        if (!isLoggedIn(id)) {
            connections.send(id, new Error(Message.Type.POST.ordinal()));
            return;
        }
        Set<Integer> toSend = new HashSet<>(clients.get(id).getFollowers());
        LinkedList<String> taggedUsers = post.getTaggedUsers();

        // add all registered taggedUsers users to toSend
        for (String username : taggedUsers) {
            if (usernames.containsKey(username)) {
                int taggedId = usernames.get(username);
                Client tagged = clients.get(taggedId);
                if (!tagged.isBlocked(id) & !me.isBlocked(taggedId)) toSend.add(taggedId);
            }
        }

        postsAndPm.add(post.getContent());
        connections.send(id, new Ack(Message.Type.POST.ordinal()));

        // send to all relevant users
        String postingUser = clients.get(id).getUserName();
        for (int i : toSend)
            connections.send(i, new Notification(Message.Type.POST, postingUser, post.getContent()));
    }

    public void handlePM(PM pm) {
        int id = pm.getId();
        Client me = clients.get(id);
        int recipientId = -1;
        Client recipient = null;
        if (usernames.containsKey(pm.getRecipient())) {
            recipientId = usernames.get(pm.getRecipient());
            recipient = clients.get(recipientId);
        }
        if (!isLoggedIn(id)) connections.send(id, new Error(Message.Type.PM.ordinal()));
        else if (recipient == null) // recipient is not registered
            connections.send(id, new Error(Message.Type.PM.ordinal(), "@" + pm.getRecipient() + " isn't applicable for private messages"));
        else if (!clients.get(recipientId).isFollower(id) || me.isBlocked(recipientId) | recipient.isBlocked(id)) // sender isn't following recipient
            connections.send(id, new Error(Message.Type.PM.ordinal())); // TODO: error??
        else {
            String filtered = filter(pm.getContent());
            postsAndPm.add(filtered);
            connections.send(id, new Ack(Message.Type.PM.ordinal()));
            connections.send(recipientId, new Notification(Message.Type.PM, clients.get(id).getUserName(), filtered));
        }
    }

    private String filter(String content) {
        for (String forbidden : filtered) {
            content = content.replaceAll(forbidden, " <filtered> ");
        }
        return content;
    }

    public void handleLogStat(LogStat logStat) {
        int id = logStat.getId();
        if (!isLoggedIn(id)) {
            connections.send(id, new Error(Message.Type.LOGSTAT.ordinal()));
            return;
        }
        for (Client client : clients.values()) {
            int currentId = client.getId();
            Client current = clients.get(currentId);
            if (currentId != id & isLoggedIn(currentId) & !current.isBlocked(id)) // doesn't get his own stat, and not of those who blocked me
                connections.send(id, singleStat(Message.Type.LOGSTAT.ordinal(), currentId));
        }
    }

    public void handleStat(Stat stat) {
        int id = stat.getId();
        if (!isLoggedIn(id)) {
            connections.send(id, new Error(Message.Type.STAT.ordinal()));
            return;
        }
        LinkedList<String> statUsernames = stat.getUsernames();
        for (String username : statUsernames) {
            if (usernames.containsKey(username)) { // send stat of registered requested users
                Client current = clients.get(usernames.get(username));
                if (!current.isBlocked(id))
                    connections.send(id, singleStat(Message.Type.STAT.ordinal(), usernames.get(username)));
            }
        }
    }

    private Ack singleStat(int opcode, int id) {
        Client client = clients.get(id); // assuming client is logged in, not blocked
        return new Ack(opcode, client.getAge(), client.getNumPosts(), client.getNumFollowers(), client.getNumFollowing());
    }

    public void handleBlock(Block block) {
        int id = block.getId();
        if (!isLoggedIn(id)) {
            connections.send(id, new Error(Message.Type.BLOCK.ordinal()));
            return;
        }
        if (usernames.containsKey(block.getToBlock())) {
            int toBlockId = usernames.get(block.getToBlock());
            Client toBlock = clients.get(toBlockId);
            Client blocking = clients.get(id);
            blocking.addBlocked(toBlockId);
            if (blocking.isFollower(toBlockId)) {
                blocking.removeFollower(toBlockId);
                toBlock.decrementFollowing();
            }
            if (toBlock.isFollower(id)) {
                toBlock.removeFollower(id);
                blocking.decrementFollowing();
            }
        }
    }

}
