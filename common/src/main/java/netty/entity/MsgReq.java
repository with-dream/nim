package netty.entity;

public class MsgReq {
    public static final int REQUEST_FRIEND = 2;
    public static final int REQUEST_FRIEND_AGREE = 4;
    public static final int REQUEST_FRIEND_REFUSE = 5;
    public static final int REQUEST_FRIEND_NOBODY = 6; //查无此人
    public static final int REQUEST_FRIEND_FRIEND = 7; //已经是好友
    public static final int FRIEND_DEL = 8; //单方删除好友
    public static final int FRIEND_DEL_EACH = 9; //删除好友 同时删除对方好友
    public static final int FRIEND_DEL_BLOCK = 10; //拉黑
    public static final int FRIEND_DEL_UNBLOCK = 11; //解除拉黑
    public static final int GROUP_CREATE = 12; //创建群
    public static final int GROUP_DEL = 13; //删除群
    public static final int GROUP_ADD = 14; //申请加入群
    public static final int GROUP_EXIT = 15; //退群
    public static final int GROUP_OUT = 16; //剔出群
    public static final int GROUP_ADD_AGREE = 17;
    public static final int GROUP_ADD_REFUSE = 18;
}
