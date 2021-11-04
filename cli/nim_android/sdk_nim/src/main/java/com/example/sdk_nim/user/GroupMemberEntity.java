package com.example.sdk_nim.user;

public class GroupMemberEntity extends Entity {
    public static final int MEMBER = 0;
    public static final int MANAGER = 1;
    public static final int OWNER = 2;

    public String uuid;
    public String groupId;
    public int role;
    public int level;
    public long insertTime;
    public long lastTime;
}