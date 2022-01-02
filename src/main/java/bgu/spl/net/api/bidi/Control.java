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
    private final ConcurrentHashMap<String, Client> usernameToClient = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Client> idToClient = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String> postsAndPm = new ConcurrentLinkedQueue<>();

    public Control(Connections<Message> connections) {
        this.connections = connections;
    }

    private boolean isRegistered(String username) {
        return usernameToClient.containsKey(username);
    }

    private boolean isLoggedIn(int connId) {
        return idToClient.containsKey(connId);
    }

    public void handleRegister(Register register) {
        String username = register.getUserName();
        int connId = register.getConnId();
        if (isRegistered(username))
            connections.send(connId, new Error((short) Message.Type.REGISTER.ordinal()));

        Client client = new Client(connId, register.getUserName(), register.getPassword(), register.getAge());
        usernameToClient.put(username, client);
        connections.send(connId, new Ack((short) Message.Type.REGISTER.ordinal()));
    }

    public void handleLogin(Login login) {
        int connId = login.getConnId();
        Client client = usernameToClient.get(login.getUsername());
        if (!isRegistered(client.getUserName()) || isLoggedIn(connId) ||
                login.getCapcha() == 0 | !login.getPassword().equals(usernameToClient.get(client.getUserName()).getPassword())) {
            connections.send(connId, new Error((short) Message.Type.LOGIN.ordinal()));
            return;
        }
        idToClient.put(connId, client);
        client.logIn();
        client.setConnId(connId);
        connections.send(connId, new Ack((short) Message.Type.LOGIN.ordinal()));
        ConcurrentLinkedQueue<Notification> notifications = client.getNotifications();
        while (!notifications.isEmpty()) connections.send(connId, notifications.remove());
    }

    public void handleLogout(Logout logout) {
        int connId = logout.getConnId();
        if (!isLoggedIn(connId)) {
            connections.send(connId, new Error((short) Message.Type.LOGOUT.ordinal()));
            return;
        }
        idToClient.get(connId).logOut();
        idToClient.remove(connId);
        connections.send(connId, new Ack((short) Message.Type.LOGOUT.ordinal()));
        connections.disconnect(connId);
    }

    public void handleFollow(Follow follow) {
        int connId = follow.getConnId();
        Client me = idToClient.get(connId);
        Client toFollow = usernameToClient.get(follow.getUserToFollow());
        if (!isLoggedIn(connId) | !isRegistered(toFollow.getUserName())) {
            connections.send(connId, new Error((short) Message.Type.FOLLOW.ordinal()));
            return;
        }

        ConcurrentLinkedQueue<Client> followers = me.getFollowers();
        if (follow.followAction()) { // case follow
            if (followers.contains(toFollow) | me.isBlocked(toFollow) | toFollow.isBlocked(me))
                connections.send(connId, new Error((short) Message.Type.FOLLOW.ordinal()));
            else {
                me.incrementFollowing();
                toFollow.addFollower(me);
                connections.send(connId, new Ack((short) Message.Type.FOLLOW.ordinal(), 0, follow.getUserToFollow()));
            }
        } else { // case unfollow
            if (!followers.contains(toFollow))
                connections.send(connId, new Error((short) Message.Type.FOLLOW.ordinal()));
            else {
                me.decrementFollowing();
                toFollow.removeFollower(me);
                connections.send(connId, new Ack((short) Message.Type.FOLLOW.ordinal(), 1, follow.getUserToFollow()));
            }
        }
    }

    public void handlePost(Post post) {
        int connId = post.getConnId();
        Client me = idToClient.get(connId);
        if (!isLoggedIn(connId)) {
            connections.send(connId, new Error((short) Message.Type.POST.ordinal()));
            return;
        }
        Set<Client> toSend = new HashSet<>(me.getFollowers());
        LinkedList<String> taggedUsers = post.getTaggedUsers();

        // add all registered taggedUsers users to toSend
        for (String username : taggedUsers) {
            if (isRegistered(username)) {
                Client tagged = usernameToClient.get(username);
                if (!tagged.isBlocked(me) & !me.isBlocked(tagged)) toSend.add(tagged);
            }
        }

        postsAndPm.add(post.getContent());
        connections.send(connId, new Ack((short) Message.Type.POST.ordinal()));

        // send to all relevant users
        String postingUser = me.getUserName();
        for (Client c : toSend) {
            Notification notification = new Notification(Message.Type.POST, postingUser, post.getContent());
            if (c.isLoggedIn())
                connections.send(c.getConnId(), notification);
            else c.addNotification(notification);
        }
    }

    public void handlePM(PM pm) {
        int connId = pm.getConnId();
        Client me = idToClient.get(connId);
        if (!isLoggedIn(connId)) { // sender is not logged in
            connections.send(connId, new Error((short) Message.Type.PM.ordinal()));
            return;
        }
        if (!isRegistered(pm.getRecipient())) { // recipient is not registered
            connections.send(connId, new Error((short) Message.Type.PM.ordinal(), "@" + pm.getRecipient() + " isn't applicable for private messages"));
            return;
        }
        Client recipient = usernameToClient.get(pm.getRecipient());
        int recipientConnId = recipient.getConnId();
        if (!recipient.isFollower(me) || me.isBlocked(recipient) | recipient.isBlocked(me)) { // sender isn't following recipient or blocked
            connections.send(connId, new Error((short) Message.Type.PM.ordinal()));
            return;
        }
        String filtered = filter(pm.getContent());
        postsAndPm.add(filtered);
        connections.send(connId, new Ack((short) Message.Type.PM.ordinal()));
        Notification notification = new Notification(Message.Type.PM, me.getUserName(), filtered);
        if (recipient.isLoggedIn())
            connections.send(recipientConnId, notification);
        else recipient.addNotification(notification);
    }

    private String filter(String content) {
        for (String forbidden : filtered) {
            content = content.replaceAll(forbidden, " <filtered> ");
        }
        return content;
    }

    public void handleLogStat(LogStat logStat) {
        int connId = logStat.getConnId();
        if (!isLoggedIn(connId)) {
            connections.send(connId, new Error((short) Message.Type.LOGSTAT.ordinal()));
            return;
        }
        Client me = idToClient.get(connId);
        for (Client client : idToClient.values()) {
            if (!me.equals(client) & !client.isBlocked(me)) // doesn't get his own stat, and not of those who blocked me
                connections.send(connId, singleStat((short) Message.Type.LOGSTAT.ordinal(), client));
        }
    }

    public void handleStat(Stat stat) {
        int connId = stat.getConnId();
        if (!isLoggedIn(connId)) {
            connections.send(connId, new Error((short) Message.Type.STAT.ordinal()));
            return;
        }
        Client me = idToClient.get(connId);
        LinkedList<String> statUsernames = stat.getUsernames();
        for (String username : statUsernames) {
            if (isRegistered(username)) { // send stat of registered requested users
                Client current = usernameToClient.get(username);
                if (!current.isBlocked(me))
                    connections.send(connId, singleStat((short) Message.Type.STAT.ordinal(), current));
            }
        }
    }

    private Ack singleStat(short opcode, Client client) { // assuming client is logged in, not blocked
        return new Ack(opcode, client.getAge(), client.getNumPosts(), client.getNumFollowers(), client.getNumFollowing());
    }

    public void handleBlock(Block block) {
        int connId = block.getConnId();
        if (!isLoggedIn(connId)) {
            connections.send(connId, new Error((short) Message.Type.BLOCK.ordinal()));
            return;
        }
        if (isRegistered(block.getToBlock())) {
            Client toBlock = usernameToClient.get(block.getToBlock());
            Client me = idToClient.get(connId);
            if (me.isBlocked(toBlock)) {
                connections.send(connId, new Error((short) Message.Type.BLOCK.ordinal()));
                return;
            }
            me.addBlocked(toBlock);
            if (me.isFollower(toBlock)) {
                me.removeFollower(toBlock);
                toBlock.decrementFollowing();
            }
            if (toBlock.isFollower(me)) {
                toBlock.removeFollower(me);
                me.decrementFollowing();
            }
            connections.send(connId, new Ack((short) Message.Type.BLOCK.ordinal()));
        }
    }

}
