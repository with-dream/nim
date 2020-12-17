package user;

public class FriendModel {
    //status字段 1 双向普通好友 2 user是friend的单向好友 3 friend是user的单向好友 4 假删除好友 5 user拉黑friend  6 friend拉黑user 7 互相拉黑
    public static final int FRIEND_NORMAL = 1;
    public static final int FRIEND_SELF = 2;
    public static final int FRIEND_OTHER = 3;
    public static final int FRIEND_DEL = 4;
    public static final int FRIEND_BLOCK_SELF = 5;
    public static final int FRIEND_BLOCK_OTHER = 6;
    public static final int FRIEND_BLOCK = 7;

    public String userId;
    public String friendId;
    public int status;
    public String userInfo;
    public String friendInfo;

    @Override
    public String toString() {
        return "FriendModel{" +
                "userId=" + userId +
                ", friendId=" + friendId +
                ", status=" + status +
                ", userInfo='" + userInfo + '\'' +
                ", friendInfo='" + friendInfo + '\'' +
                '}';
    }
}
