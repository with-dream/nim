package netty.model;

public class CmdMsgModel extends BaseMsgModel {
    public static final int LOGIN = 0;
    public static final int LOGOUT = 1;
    public static final int HEART = 3;

    public static final int RECEIVED = 10;
    public static final int READED = 11;

    public int cmd;

    public static CmdMsgModel create(long from, long to) {
        CmdMsgModel cmdMsgModel = new CmdMsgModel();
        cmdMsgModel.type = MsgType.CMD_MSG;
        cmdMsgModel.from = from;
        cmdMsgModel.to = to;

        return cmdMsgModel;
    }

    @Override
    public String toString() {
        return "CmdMsgModel{" +
                "cmd=" + cmd +
                ", type=" + type +
                ", seq=" + seq +
                ", timeLine=" + timeLine +
                ", msgId='" + msgId + '\'' +
                ", device=" + device +
                ", from=" + from +
                ", to=" + to +
                ", timestamp=" + timestamp +
                '}';
    }
}
