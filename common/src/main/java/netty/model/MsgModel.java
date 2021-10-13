package netty.model;

public class MsgModel extends BaseMsgModel {
    public String info;
    public int cmd;

    public static MsgModel createP(String from, String to, int clientToken) {
        MsgModel msgModel = new MsgModel();
        msgModel.createMsgId();
        msgModel.from = from;
        msgModel.to = to;
        msgModel.fromToken = clientToken;
        msgModel.type = MsgType.MSG_PERSON;
        return msgModel;
    }

    @Override
    public String toString() {
        return "MsgCmd{" +
                "info='" + info + '\'' +
                ", type=" + type +
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
