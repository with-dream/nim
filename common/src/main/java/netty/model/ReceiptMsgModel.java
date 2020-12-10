package netty.model;

public class ReceiptMsgModel extends CmdMsgModel {
    public static final int FORCE = 1000;
    public static final int CMD = 800;
    public static final int MSG = 500;

    public String receipt;
    public int level;   //回执消息的权重

    public static ReceiptMsgModel create(long from, long to, String receipt) {
        ReceiptMsgModel recMsg = new ReceiptMsgModel();
        recMsg.createMsgid();
        recMsg.receipt = receipt;
        recMsg.type = MsgType.RECEIPT_MSG;
        recMsg.from = from;
        recMsg.to = to;

        return recMsg;
    }
}
