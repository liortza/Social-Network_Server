package bgu.spl.net.api.messages;

public class Login extends Message {
    String username, password;
    byte capcha;

    public Login(int id, String username, String password, String capcha) {
        super(Type.LOGIN, id);
        this.username = username;
        this.password = password;
        if (capcha.equals("0")) this.capcha = 0;
        else this.capcha = 1;
    }

    public byte getCapcha() {
        return capcha;
    }

    public String getPassword() {
        return password;
    }
}
