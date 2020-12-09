package netty.model;

public class MsgType {
    /**
     * 命令消息
     */
    public static final int CMD_MSG = 0;

    /**
     * 请求的命令消息
     */
    public static final int REQ_CMD_MSG = 5;

    /**
     * 请求的命令消息
     */
    public static final int REQ_GROUP_MSG = 6;

    /**
     * 普通消息
     */
    public static final int MSG_PERSON = 1;

    /**
     * 群消息
     */
    public static final int MSG_GROUP = 2;

    /**
     * 系统消息
     */
    public static final int MSG_ROOT = 3;

    /**
     * 回执消息
     */
    public static final int RECEIPT_MSG = 4;

}
