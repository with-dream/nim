package netty.entity;

/**
 * Message的msgType以MSG_TYPE_XX格式 用于标记消息的类型
 * <p>
 * MSG_KEY_MSG_XX 用于msg字段的KEY
 * MSG_KEY_REC_XX 用于receipt字段的key
 * INTERNAL_KEY_UNIFY_SERVICE_XX 用于服务器内部 客户端可以忽略
 */
public class MsgType {
    /**
     * 系统消息
     */
    public static final int TYPE_ROOT = 0;

    /**
     * 请求的命令消息
     */
    public static final int TYPE_CMD = 1;
    public static final int KEY_CMD = 101; //cmd命令 参照MsgCmd
    public static final int KEY_EXTRA = 102; //cmd命令的附加信息

    /**
     * 请求的群命令消息
     */
    public static final int TYPE_CMD_GROUP = 2;
    public static final int KEY_CMD_GROUP = 202;
    public static final int KEY_EXTRA_GROUP = 203;

    /**
     * 普通消息
     */
    public static final int TYPE_MSG = 3;
    public static final int KEY_MSG = 301; //普通消息

    /**
     * 群消息
     */
    public static final int TYPE_GROUP = 4;
    public static final int KEY_MSG_GROUP = 402; //普通消息

    /**
     * 回执消息
     */
    public static final int TYPE_RECEIPT = 5;
    public static final int KEY_RECEIPT_MSG_ID = 501; //接收到的消息id
    public static final int KEY_RECEIPT_TYPE = 502; //回执类型
    public static final int KEY_RECEIPT_STATE = 503; //消息的状态

    /**
     * 打包消息
     */
    public static final int TYPE_PACK = 6;
    public static final int KEY_PACK = 601; //普通消息


    public static final int KEY_UNIFY_GROUP_ID = 10001;//群id
    public static final int KEY_UNIFY_MSG_TOKEN = 10002;//消息的token 用于消息回执

    public static final int KEY_UNIFY_CLIENT_SEND_TIME = 20001;//客户端发送时间
    public static final int KEY_UNIFY_CLIENT_SEND_CHANNEL = 20002;//客户端channel
    public static final int KEY_UNIFY_CLIENT_MSG_TOKEN = 20003;//客户端的临时token

    public static final int KEY_UNIFY_SERVICE_SEND_TIME = 30001;//服务器端发送时间
    public static final int KEY_UNIFY_SERVICE_SEND_CHANNEL = 30002;//服务器channel
    public static final int KEY_UNIFY_SERVICE_MSG_TOKEN = 30003;//服务端的临时token
    public static final int KEY_UNIFY_SERVICE_GROUP_UUID_LIST = 30004; //服务器集群时 同一条消息需要发送到同一台服务器的uuid集合 使用HSet<String>
}
