package bgu.spl.net.api.messages;

public class Register extends Message {

    private final String userName, password;
    private short age;

    public Register(int connId, String userName, String password, String bday) {
        super(Type.REGISTER, connId);
        this.userName = userName;
        this.password = password;
        String year = bday.substring(6);
        age = (short) ((short) 2022 - Integer.parseInt(year));
    }

    public String getUserName() { return userName; }

    public String getPassword() { return password; }

    public short getAge() { return age; }
}
