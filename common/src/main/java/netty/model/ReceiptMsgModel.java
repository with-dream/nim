package netty.model;

public class ReceiptMsgModel extends MsgModel {
    public static final int FORCE = 1000;
    public static final int CMD = 800;
    public static final int MSG = 500;

    public long sendMsgId;  //发送数据时的msgId
    public int sendMsgType; //发送消息的类型

    public String token() {
        return sendMsgId + "" + fromToken;
    }

    public static ReceiptMsgModel create(String from, String to, long sendMsgId, int clientToken) {
        ReceiptMsgModel recMsg = new ReceiptMsgModel();
        recMsg.createMsgId();
        recMsg.sendMsgId = sendMsgId;
        recMsg.type = MsgType.MSG_RECEIPT;
        recMsg.from = from;
        recMsg.to = to;
        recMsg.fromToken = clientToken;

        return recMsg;
    }
}
