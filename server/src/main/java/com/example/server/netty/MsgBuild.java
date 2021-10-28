package com.example.server.netty;

import netty.entity.MsgType;
import netty.entity.NimMsg;
import utils.UUIDUtil;

public class MsgBuild {
    public static NimMsg build(String from, String to, int msgType) {
        NimMsg msg = new NimMsg();
        msg.sync();

        msg.msgId = UUIDUtil.getMsgId();
        msg.from = from;
        msg.to = to;
        msg.msgType = msgType;
        return msg;
    }

    public static NimMsg recMsg(String from, String to) {
        return build(from, to, MsgType.TYPE_RECEIPT);
    }

    public static NimMsg recMsg(NimMsg msg) {
        NimMsg newMsg = recMsg(msg.to, msg.from);
        newMsg.receipt.putAll(msg.receipt);
        newMsg.msgMap().put(MsgType.KEY_M_RECEIPT_TYPE, msg.msgType);
        newMsg.msgMap().put(MsgType.KEY_M_RECEIPT_MSG_ID, msg.msgId);
        newMsg.msgMap().put(MsgType.KEY_M_RECEIPT_STATE, MsgType.STATE_RECEIPT_SERVER_SUCCESS);
        return newMsg;
    }
}
