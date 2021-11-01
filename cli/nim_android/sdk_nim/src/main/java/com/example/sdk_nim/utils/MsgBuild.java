package com.example.sdk_nim.utils;

import com.example.sdk_nim.netty.IMContext;
import com.example.sdk_nim.netty.entity.MsgType;
import com.example.sdk_nim.netty.entity.NimMsg;

public class MsgBuild {

    public static NimMsg build(int msgType, String to) {
        NimMsg msg = new NimMsg();
        msg.msgId = UUIDUtil.getMsgId();
        msg.from = IMContext.instance().uuid;
        msg.fromToken = IMContext.instance().clientToken;
        msg.to = to;
        msg.msgType = msgType;
        msg.deviceType = Constant.ANDROID;
        msg.sync();
        return msg;
    }

    public static NimMsg recMsg(String to) {
        return build(MsgType.TYPE_RECEIPT, to);
    }

    public static NimMsg heart() {
        return build(MsgType.TYPE_HEART_PING, Constant.SERVER_UID);
    }
}
