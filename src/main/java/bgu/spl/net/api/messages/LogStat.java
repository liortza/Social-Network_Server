package bgu.spl.net.api.messages;

public class LogStat extends Message {
    public LogStat(int connId) {
        super(Type.LOGSTAT, connId);
    }
}
