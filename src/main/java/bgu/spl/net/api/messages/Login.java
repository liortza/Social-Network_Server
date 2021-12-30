package bgu.spl.net.api.messages;

public class Login extends Message {
    String username, password;
    byte capcha;

    public Login(int id, String username, String password, byte capcha) {
        super(Type.LOGIN, id);
        this.username = username;
        this.password = password;
        this.capcha = capcha;
    }

    public byte getCapcha() {
        return capcha;
    }

    public String getPassword() {
        return password;
    }
}
