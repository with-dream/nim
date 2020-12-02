package com.example.server.netty;

import io.netty.channel.Channel;
import netty.model.BaseMsgModel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SessionHolder {
    public static final ConcurrentHashMap<Long, List<SessionModel>> sessionMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Channel, Long> sessionChannelMap = new ConcurrentHashMap<>();

    public static void login(Channel channel, BaseMsgModel msgModel) {
        SessionModel sessionModel = new SessionModel();
        sessionModel.channel = channel;

        List<SessionModel> session = sessionMap.get(msgModel.from);
        if (session == null) {
            synchronized ((msgModel.from + "").intern()) {
                if (session == null) {
                    List<SessionModel> s = new LinkedList<>();
                    session = Collections.synchronizedList(s);
                    sessionMap.put(msgModel.from, session);
                }
            }
        }
        session.add(sessionModel);
        sessionChannelMap.put(channel, msgModel.from);
    }


    public static void unlogin(Channel channel) {
        Long uuid = sessionChannelMap.get(channel);
        LinkedList<SessionModel> session = (LinkedList<SessionModel>) sessionMap.get(uuid);
        session.removeIf(model -> model.channel == channel);
    }
}
