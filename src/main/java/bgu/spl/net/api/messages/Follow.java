package bgu.spl.net.api.messages;

public class Follow extends Message {
    private final int action; // 0 - follow, 1 - unfollow
    private final String userToFollow;

    public Follow(int action, String userToFollow, int id) {
        super(Type.FOLLOW, id);
        this.action = action;
        this.userToFollow = userToFollow;
    }

    public boolean followAction() { return action == 0; }

    public String getUserToFollow() { return userToFollow; }
}
