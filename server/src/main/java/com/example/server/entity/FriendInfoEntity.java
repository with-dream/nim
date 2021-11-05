package com.example.server.entity;

import entity.Entity;

import java.util.Date;

/**
 * 用户关系表
 * */
public class FriendInfoEntity extends Entity {
    public String userId;
    public String friendId;
    public boolean isFriend;
    public String remark;
    public String backImage;
    public Date insertTime;

    @Override
    public String toString() {
        return "FriendInfoEntity{" +
                "userId='" + userId + '\'' +
                ", friendId='" + friendId + '\'' +
                ", isFriend=" + isFriend +
                ", remark='" + remark + '\'' +
                ", backImage='" + backImage + '\'' +
                ", insertTime=" + insertTime +
                '}';
    }
}
