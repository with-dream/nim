package com.example.server.netty;

import io.netty.channel.Channel;
import netty.model.BaseMsgModel;
import netty.model.CmdMsgModel;
import netty.model.ReceiptModel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class SessionHolder {
    public static final ConcurrentHashMap<String, Vector<SessionModel>> sessionMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Channel, String> sessionChannelMap = new ConcurrentHashMap<>();

    public static final  ConcurrentHashMap<Integer, ReceiptModel> receiptMsg = new ConcurrentHashMap<>();

    public static void login(Channel channel, BaseMsgModel msgModel) {
        SessionModel sessionModel = new SessionModel();
        sessionModel.channel = channel;
        sessionModel.clientToken = ((CmdMsgModel) msgModel).fromToken;

        Vector<SessionModel> session = sessionMap.get(msgModel.from);
        if (session == null) {
            synchronized ((msgModel.from + "").intern()) {
                if (session == null) {
                    session = new Vector<>();
                    sessionMap.put(msgModel.from, session);
                }
            }
        }
        session.add(sessionModel);
        sessionChannelMap.put(channel, msgModel.from);
    }

    public static void logout(Channel channel) {
        String uuid = sessionChannelMap.remove(channel);
        if (uuid == null)
            return;
        Vector<SessionModel> session = sessionMap.get(uuid);
        session.removeIf(model -> model.channel == channel);
    }
}
