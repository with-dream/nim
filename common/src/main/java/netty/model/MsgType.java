package netty.model;

public class MsgType {
    /**
     * 命令消息
     */
    public static final int MSG_CMD = 0;

    /**
     * 请求的命令消息
     */
    public static final int MSG_CMD_REQ = 5;

    /**
     * 请求的命令消息
     */
    public static final int MSG_GROUP_REQ = 6;

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
    public static final int MSG_RECEIPT = 4;

    /**
     * 打包消息
     */
    public static final int MSG_PACK = 7;

}
