package netty.model;

public class ReceiptMsgModel extends CmdMsgModel {
    public String receipt;
    public int level;   //回执消息的权重
}
