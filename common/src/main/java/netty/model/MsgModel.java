package netty.model;

public class MsgModel extends BaseMsgModel {
    public String info;

    public static MsgModel createP(long from, long to) {
        MsgModel msgModel = new MsgModel();
        msgModel.from = from;
        msgModel.to = to;
        msgModel.type = MsgType.MSG_PERSON;
        return msgModel;
    }

    public static MsgModel createG(long from, long to) {
        MsgModel msgModel = new MsgModel();
        msgModel.from = from;
        msgModel.to = to;
        msgModel.type = MsgType.MSG_GROUP;
        return msgModel;
    }

    @Override
    public String toString() {
        return "MsgModel{" +
                "info='" + info + '\'' +
                '}';
    }
}
