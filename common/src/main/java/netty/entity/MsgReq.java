package netty.entity;

public class MsgReq {
    public static final int REQUEST_FRIEND = 2; //请求添加好友
    public static final int REQUEST_FRIEND_AGREE = 4;   //同意添加好友
    public static final int REQUEST_FRIEND_REFUSE = 5;  //拒绝添加好友
    public static final int REQUEST_FRIEND_NOBODY = 6; //查无此人
    public static final int REQUEST_FRIEND_FRIEND = 7; //已经是好友
    public static final int REQUEST_FRIEND_BLOCK = 8; //已经是黑名单 无法加好友
    public static final int FRIEND_DEL = 50; //单方删除好友
    public static final int FRIEND_DEL_EACH = 51; //删除好友 同时删除对方好友
    public static final int FRIEND_DEL_BLOCK = 52; //拉黑
    public static final int FRIEND_DEL_UNBLOCK = 53; //解除拉黑
    public static final int GROUP_CREATE = 100; //创建群
    public static final int GROUP_DEL = 101; //删除群
    public static final int GROUP_ADD = 102; //申请加入群
    public static final int GROUP_EXIT = 103; //退群
    public static final int GROUP_OUT = 104; //剔出群
    public static final int GROUP_ADD_AGREE = 105;
    public static final int GROUP_ADD_REFUSE = 106;
}
