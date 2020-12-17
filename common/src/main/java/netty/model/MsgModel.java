package netty.model;

import java.io.Serializable;

public class MsgModel extends BaseMsgModel implements Serializable {
    public String info;
    public String groupId;

    public static MsgModel createP(String from, String to, int clientToken) {
        MsgModel msgModel = new MsgModel();
        msgModel.createMsgId();
        msgModel.from = from;
        msgModel.to = to;
        msgModel.fromToken = clientToken;
        msgModel.type = MsgType.MSG_PERSON;
        return msgModel;
    }

    public static MsgModel createG(String from, String to, int clientToken) {
        MsgModel msgModel = new MsgModel();
        msgModel.createMsgId();
        msgModel.from = from;
        msgModel.groupId = to;
        msgModel.fromToken = clientToken;
        msgModel.type = MsgType.MSG_GROUP;
        return msgModel;
    }

    @Override
    public String toString() {
        return "MsgModel{" +
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
                ", queueName='" + queueName + '\'' +
                '}';
    }
}
