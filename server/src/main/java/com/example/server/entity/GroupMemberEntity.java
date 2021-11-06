package com.example.server.entity;

import entity.Entity;

import java.util.Date;

public class GroupMemberEntity extends Entity {
    public static final int MEMBER = 0;
    public static final int MANAGER = 1;
    public static final int OWNER = 2;

    public String uuid;
    public String groupId;
    public int folder;
    public int role;
    public int level;
    public String groupRemark;
    public String nickName;
    public int noticeType;
    public Date insertTime;
    public Date lastTime;
}
