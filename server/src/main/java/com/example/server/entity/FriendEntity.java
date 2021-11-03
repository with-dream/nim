package com.example.server.entity;

import entity.Entity;

public class FriendEntity extends Entity {
    public static final int FRIEND = 1;
    public static final int FRIEND_UN = 2;
    public static final int BLOCK = 3;
    public static final int BLOCK_UN = 4;

    public String userId;
    public String friendId;
    public int friend;
    public int block;
    public boolean isFriend;
    public boolean isBlock;

    @Override
    public String toString() {
        return "FriendEntity{" +
                "userId=" + userId +
                ", friendId=" + friendId +
                ", friend=" + friend +
                ", block=" + block +
                '}';
    }
}
