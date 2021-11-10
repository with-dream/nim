package com.example.server.entity;

import entity.Entity;

public class RequestEntity extends Entity {
    public static final int FRIEND = 1;
    public static final int GROUP = 2;

    public String userId;
    public String friendId;
    public String extra;
    public int source;
    public int folder;
    public int status;
    public int targetType;
}
