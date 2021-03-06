package bgu.spl.net.srv;

import bgu.spl.net.api.messages.Notification;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {
    private int connId;
    private boolean loggedIn;
    private final String userName;
    private final String password;
    private final short age;
    private final ConcurrentLinkedQueue<Client> followers;
    private short following;
    private final ConcurrentLinkedQueue<Client> blocked;
    private short numPosts;
    private final ConcurrentLinkedQueue<Notification> notifications;

    public Client(int connId, String userName, String password, short age) {
        this.connId = connId;
        loggedIn = false;
        this.userName = userName;
        this.password = password;
        this.age = age;
        followers = new ConcurrentLinkedQueue<>();
        blocked = new ConcurrentLinkedQueue<>();
        following = 0;
        numPosts = 0;
        notifications = new ConcurrentLinkedQueue<>();
    }

    public int getConnId() {
        return connId;
    }

    public void setConnId(int connId) { this.connId = connId; }

    public String getUserName() {
        return userName;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void logIn() {
        loggedIn = true;
    }

    public void logOut() {
        loggedIn = false;
    }

    public String getPassword() {
        return password;
    }

    public short getAge() {
        return age;
    }

    public short getNumPosts() {
        return numPosts;
    }

    public void incrementPosts() {
        numPosts++;
    }

    public void incrementFollowing() {
        following++;
    }

    public void decrementFollowing() {
        following--;
    }

    public boolean isFollower(Client client) { return followers.contains(client); }

    public ConcurrentLinkedQueue<Client> getFollowers() {
        return followers;
    }

    public short getNumFollowers() {
        return (short) followers.size();
    }

    public void addFollower(Client client) {
        followers.add(client);
    }

    public void removeFollower(Client toRemove) {
        followers.remove(toRemove);
    }

    public short getNumFollowing() {
        return following;
    }

    public void addBlocked(Client blocked) {
        this.blocked.add(blocked);
    }

    public boolean isBlocked(Client client) {
        return blocked.contains(client);
    }

    public void addNotification(Notification n) {
        notifications.add(n);
    }

    public ConcurrentLinkedQueue<Notification> getNotifications() {
        return notifications;
    }

}
