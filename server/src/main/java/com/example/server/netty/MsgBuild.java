package com.example.server.netty;

import netty.entity.MsgLevel;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import utils.UUIDUtil;

public class MsgBuild {
    public static NimMsg build(String from, String to, int msgType, int level) {
        NimMsg msg = new NimMsg();
        msg.sync();

        msg.msgId = UUIDUtil.getMsgId();
        msg.from = from;
        msg.to = to;
        msg.msgType = msgType;
        msg.level = level;
        return msg;
    }

    public static NimMsg recMsg(String from, String to) {
        return build(from, to, MsgType.TYPE_RECEIPT, MsgLevel.LEVEL_NORMAL);
    }

    public static NimMsg recMsg(NimMsg msg) {
        NimMsg newMsg = recMsg(msg.to, msg.from);
        newMsg.receipt.putAll(msg.receipt);
        newMsg.recMap().put(MsgType.KEY_RECEIPT_TYPE, msg.msgType);
        newMsg.recMap().put(MsgType.KEY_RECEIPT_MSG_ID, msg.msgId);
        newMsg.recMap().put(MsgType.KEY_RECEIPT_STATE, MsgType.STATE_RECEIPT_SERVER_SUCCESS);
        return newMsg;
    }
}
