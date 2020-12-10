package netty.model;

public class MsgModel extends BaseMsgModel {
    public String info;

    public static MsgModel createP(long from, long to, String receiptTag) {
        MsgModel msgModel = new MsgModel();
        msgModel.createMsgid();
        msgModel.from = from;
        msgModel.to = to;
        msgModel.receiptTag = receiptTag;
        msgModel.type = MsgType.MSG_PERSON;
        return msgModel;
    }

    public static MsgModel createG(long from, long to, String receiptTag) {
        MsgModel msgModel = new MsgModel();
        msgModel.createMsgid();
        msgModel.from = from;
        msgModel.to = to;
        msgModel.receiptTag = receiptTag;
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
                ", msgId='" + msgId + '\'' +
                ", device=" + device +
                ", receiptTag='" + receiptTag + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", timestamp=" + timestamp +
                ", tryCount=" + tryCount +
                '}';
    }
}
