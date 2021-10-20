package com.example.imlib.utils;

import com.example.imlib.netty.IMContext;
import netty.entity.MsgLevel;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import utils.Constant;
import utils.UUIDUtil;

public class MsgBuild {
    public static NimMsg build(int msgType, String to) {
        return build(msgType, to, MsgLevel.LEVEL_NORMAL);
    }

    public static NimMsg build(int msgType, String to, int level) {
        NimMsg msg = new NimMsg();
        msg.msgId = UUIDUtil.getMsgId();
        msg.from = IMContext.instance().uuid;
        msg.fromToken = IMContext.instance().clientToken;
        msg.to = to;
        msg.msgType = msgType;
        msg.deviceType = Constant.ANDROID;
        msg.level = level;
        msg.sync();
        return msg;
    }

    public static NimMsg recMsg(String to) {
        return build(MsgType.TYPE_RECEIPT, to, MsgLevel.LEVEL_NORMAL);
    }

    public static NimMsg heart() {
        return build(MsgType.TYPE_HEART_PING, Constant.SERVER_UID, MsgLevel.LEVEL_LOW);
    }
}
