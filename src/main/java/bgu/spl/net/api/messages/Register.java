package bgu.spl.net.api.messages;

public class Register extends Message {

    private final String userName, password;
    private int age;

    public Register(int id, String userName, String password, String bday) {
        super(Type.REGISTER, id);
        this.userName = userName;
        this.password = password;
        // TODO: age <- bday
    }

    public String getUserName() { return userName; }

    public String getPassword() { return password; }

    public int getAge() { return age; }
}
