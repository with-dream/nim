package netty.model;

public class ReqGroupMsgModel extends RequestMsgModel {
    public int groupId;

    public static ReqGroupMsgModel create(long from, long to) {
        ReqGroupMsgModel reqMsgModel = new ReqGroupMsgModel();
        reqMsgModel.type = MsgType.REQ_GROUP_MSG;
        reqMsgModel.from = from;
        reqMsgModel.to = to;

        return reqMsgModel;
    }

    @Override
    public String toString() {
        return "ReqGroupMsgModel{" +
                "groupId=" + groupId +
                ", cmd=" + cmd +
                ", status=" + status +
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
