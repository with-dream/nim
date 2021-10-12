package com.example.server.entity;

import netty.model.MsgModel;
import netty.model.MsgType;

import java.util.HashSet;
import java.util.Set;

public class GroupMsgModel extends MsgModel {
    //消息的目标uuid
    public Set<String> toSet = new HashSet<>();

    public static GroupMsgModel createG(String from, String to) {
        GroupMsgModel msgModel = new GroupMsgModel();
        msgModel.createMsgId();
        msgModel.from = from;
        msgModel.groupId = to;
        msgModel.type = MsgType.MSG_GROUP;
        return msgModel;
    }
}
