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
    public static final String CACHE_ROOT = "root";

    /**
     * 请求的命令消息
     */
    public static final int TYPE_CMD = 1;
    public static final String CACHE_CMD = "cmd";
    public static final int KEY_CMD = 101; //cmd命令 参照MsgCmd
    public static final int KEY_EXTRA = 102; //cmd命令的附加信息
    public static final int KEY_M_REQ_EXTRA = 110; //请求好友数据 见RequestEntity
    public static final int KEY_M_USER_INFO_EXTRA = 111; //好友申请确认数据 见UserInfoEntity

    /**
     * 请求的群命令消息
     */
    public static final int TYPE_CMD_GROUP = 2;
    public static final String CACHE_CMD_GROUP = "cmd_g";
    public static final int KEY_M_CMD_GROUP = 202;
    public static final int KEY_M_EXTRA_GROUP = 203;

    /**
     * 普通消息
     */
    public static final int TYPE_MSG = 3;
    public static final String CACHE_MSG = "msg";
    public static final int KEY_M_MSG = 301; //普通消息

    /**
     * 群消息
     */
    public static final int TYPE_GROUP = 4;
    public static final String CACHE_GROUP = "msg_g";
    public static final int KEY_M_MSG_GROUP = 402; //普通消息

    /**
     * 回执消息
     */
    public static final int TYPE_RECEIPT = 5;
    public static final String CACHE_RECEIPT = "rec";
    public static final int KEY_M_RECEIPT_SERVICE = 500;//来自服务器的回执消息
    public static final int KEY_M_RECEIPT_CLIENT = 501;//来自目标客户端的回执消息
    public static final int KEY_M_RECEIPT_MSG_ID = 503; //目标客户端接收到的消息id回执到发送端
    public static final int KEY_M_RECEIPT_TYPE = 504; //回执类型
    public static final int KEY_M_RECEIPT_STATE = 505; //消息的状态
    public static final int STATE_RECEIPT_CLIENT_SUCCESS = 0; //消息的状态 客户端接收成功
    public static final int STATE_RECEIPT_SERVER_SUCCESS = 1; //消息的状态 服务器接收成功
    public static final int STATE_RECEIPT_OFFLINE = 5; //消息的状态 目标客户端离线
    public static final int STATE_RECEIPT_FAIL = 6; //消息的状态 发送到目标客户端失败
    public static final int KEY_M_RECEIPT_CMD_EXTRA_CODE = 506; //cmd消息的返回状态
    public static final int KEY_M_RECEIPT_CMD_EXTRA_MSG = 507; //cmd消息的返回信息


    /**
     * 打包消息
     */
    public static final int TYPE_PACK = 6;
    public static final int KEY_M_PACK = 601; //普通消息

    /**
     * 心跳消息
     */
    public static final int TYPE_HEART_PING = 20;
    public static final int TYPE_HEART_PONG = 21;

    public static final int KEY_UNIFY_M_GROUP_ID = 10001;//群id  string
    public static final int KEY_UNIFY_M_REC_DIRECT = 10005;//服务器-客户端/客户端-服务器间的回执 bool
    public static final int KEY_UNIFY_M_REC_CLIENT = 10006;//客户端-客户端间的回执 bool

    public static final int KEY_UNIFY_CLIENT_SEND_TIME = 20001;//客户端发送时间
    public static final int KEY_UNIFY_CLIENT_SEND_CHANNEL = 20002;//客户端channel
    public static final int KEY_UNIFY_CLIENT_MSG_TOKEN = 20003;//客户端的临时token
    public static final int KEY_UNIFY_M_CLIENT_SEND_SELF = 20005;//消息是否需要转发给发送者的其他客户端 用于消息同步

    public static final int KEY_UNIFY_R_SERVICE_MSG_TOKEN = 30003;//服务端的临时token
    public static final int KEY_UNIFY_R_SERVICE_UUID_LIST = 30004; //服务器集群时 同一条消息需要发送到同一台服务器的多个uuid(群消息) 使用List<String> 需要确保只发送一次

    //离线消息
    public static final String CACHE_OFFLINE_MSG = "off";
}
