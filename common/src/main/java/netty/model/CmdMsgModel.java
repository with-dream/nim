package netty.model;

public class CmdMsgModel extends BaseMsgModel {
    public static final int LOGIN = 0;
    public static final int LOGOUT = 1;
    public static final int RECEIVED = 10;
    public static final int READED = 11;

    public int cmd;
}
