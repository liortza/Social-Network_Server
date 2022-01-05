package bgu.spl.net.api.messages;

public class Login extends Message {
    String username, password;
    byte capcha;

    public Login(int connId, String username, String password, String capcha) {
        super(Type.LOGIN, connId);
        this.username = username;
        this.password = password;
        if (capcha.equals("0")) this.capcha = 0;
        else this.capcha = 1;
    }

    public byte getCapcha() {
        return capcha;
    }

    public String getUsername() { return username; }

    public String getPassword() {
        return password;
    }
}
