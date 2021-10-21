package netty.entity;

public class MsgCmd {
    public static final int LOGIN = 0;
    public static final int LOGOUT = 1;

    public static final int REQUEST_FRIEND = 1001; //请求添加好友
    public static final int REQUEST_FRIEND_AGREE = 1002;   //同意添加好友
    public static final int REQUEST_FRIEND_REFUSE = 1003;  //拒绝添加好友
    public static final int REQUEST_FRIEND_NOBODY = 1004; //查无此人
    public static final int REQUEST_FRIEND_FRIEND = 1005; //已经是好友
    public static final int REQUEST_FRIEND_BLOCK = 1006; //已经是黑名单 无法加好友
    public static final int FRIEND_DEL = 1050; //单方删除好友
    public static final int FRIEND_DEL_EACH = 1051; //删除好友 同时删除对方好友
    public static final int FRIEND_DEL_BLOCK = 1052; //拉黑
    public static final int FRIEND_DEL_UNBLOCK = 1053; //解除拉黑
    public static final int GROUP_CREATE = 1100; //创建群
    public static final int GROUP_DEL = 1101; //删除群
    public static final int GROUP_ADD = 1102; //申请加入群
    public static final int GROUP_EXIT = 1103; //退群
    public static final int GROUP_OUT = 1104; //剔出群
    public static final int GROUP_ADD_AGREE = 1105;
    public static final int GROUP_ADD_REFUSE = 1106;
}
