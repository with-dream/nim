package com.example.server.entity;

import entity.Entity;

public class FriendModel extends Entity {
    //status字段 1 双向普通好友 2 user是friend的单向好友 3 friend是user的单向好友 4 假删除好友 5 user拉黑friend  6 friend拉黑user 7 互相拉黑
    public static final int FRIEND_NORMAL = 1;
    public static final int FRIEND_SELF = 2;
    public static final int FRIEND_OTHER = 3;
    public static final int FRIEND_DEL_EACH = 4;
    public static final int FRIEND_BLOCK_NORMAL = 1;
    public static final int FRIEND_BLOCK_SELF = 2;
    public static final int FRIEND_BLOCK_OTHER = 3;
    public static final int FRIEND_BLOCK_EACH = 4;

    public String userId;
    public String friendId;
    public int friend;
    public int block;
    public String userInfo;
    public String friendInfo;
    public boolean isFriend;
    public boolean isBlock;

    @Override
    public String toString() {
        return "FriendModel{" +
                "userId=" + userId +
                ", friendId=" + friendId +
                ", friend=" + friend +
                ", block=" + block +
                ", userInfo='" + userInfo + '\'' +
                ", friendInfo='" + friendInfo + '\'' +
                '}';
    }
}
