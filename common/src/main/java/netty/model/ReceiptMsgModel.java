package netty.model;

import java.io.Serializable;

public class ReceiptMsgModel extends CmdMsgModel implements Serializable {
    public static final int FORCE = 1000;
    public static final int CMD = 800;
    public static final int MSG = 500;

    public long sendMsgId;  //发送数据时的msgId
    public int sendMsgType; //发送消息的类型
    public int level;   //回执消息的权重

    public static ReceiptMsgModel create(String from, String to, long sendMsgId, int clientToken) {
        ReceiptMsgModel recMsg = new ReceiptMsgModel();
        recMsg.createMsgId();
        recMsg.sendMsgId = sendMsgId;
        recMsg.type = MsgType.RECEIPT_MSG;
        recMsg.from = from;
        recMsg.to = to;
        recMsg.fromToken = clientToken;

        return recMsg;
    }
}
