package bgu.spl.net.srv;

import bgu.spl.net.api.messages.Notification;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {
    private final int id;
    private boolean loggedIn;
    private final String userName;
    private final String password;
    private final int age;
    private final ConcurrentLinkedQueue<Integer> followers;
    private int following;
    private final ConcurrentLinkedQueue<Integer> blocked;
    private int numPosts;
    private final ConcurrentLinkedQueue<Notification> notifications;

    public Client(int id, String userName, String password, int age) {
        this.id = id;
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

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public boolean getLoggedIn() {
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

    public int getAge() {
        return age;
    }

    public int getNumPosts() {
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

    public boolean isFollower(int id) { return followers.contains(id); }

    public ConcurrentLinkedQueue<Integer> getFollowers() {
        return followers;
    }

    public int getNumFollowers() {
        return followers.size();
    }

    public void addFollower(int id) {
        followers.add(id);
    }

    public void removeFollower(int id) {
        followers.remove(id);
    }

    public int getNumFollowing() {
        return following;
    }

    public void addBlocked(int id) {
        blocked.add(id);
    }

    public boolean isBlocked(int id) {
        return blocked.contains(id);
    }
}
