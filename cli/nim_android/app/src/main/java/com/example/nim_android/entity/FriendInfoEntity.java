package com.example.nim_android.entity;

import com.example.sdk_nim.user.Entity;

import java.util.Date;

public class FriendInfoEntity extends Entity {
    public String userId;
    public String friendId;
    public boolean isFriend;
    public int noticeType;
    public String remark;
    public String backImage;
    public Date insertTime;

    @Override
    public String toString() {
        return "FriendInfoEntity{" +
                "userId='" + userId + '\'' +
                ", friendId='" + friendId + '\'' +
                ", isFriend=" + isFriend +
                ", noticeType=" + noticeType +
                ", remark='" + remark + '\'' +
                ", backImage='" + backImage + '\'' +
                ", insertTime=" + insertTime +
                '}';
    }
}
