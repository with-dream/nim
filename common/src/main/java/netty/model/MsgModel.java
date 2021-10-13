package netty.model;

public class MsgModel extends BaseMsgModel {
    public String info;
    public int cmd;

    public static MsgModel create(int msgType, String from, String to, int clientToken) {
        MsgModel msgModel = new MsgModel();
        msgModel.createMsgId();
        msgModel.type = msgType;
        msgModel.from = from;
        msgModel.to = to;
        msgModel.fromToken = clientToken;
        return msgModel;
    }

    public static MsgModel createCmd(String from, String to, int clientToken) {
        return create(MsgType.MSG_CMD, from, to, clientToken);
    }

    public static MsgModel createPer(String from, String to, int clientToken) {
        return create(MsgType.MSG_PERSON, from, to, clientToken);
    }

    @Override
    public String toString() {
        return "MsgModel{" +
                "info='" + info + '\'' +
                ", type=" + type +
                ", cmd=" + cmd +
                ", seq=" + seq +
                ", timeLine=" + timeLine +
                ", msgId=" + msgId +
                ", fromToken=" + fromToken +
                ", toToken=" + toToken +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", timestamp=" + timestamp +
                ", tryCount=" + tryCount +
                '}';
    }
}
