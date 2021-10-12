package com.example.server.netty;

import io.netty.channel.Channel;
import netty.model.BaseMsgModel;
import netty.model.CmdMsgModel;
import netty.model.ReceiptModel;
import utils.Constant;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 当前服务器的推送及基础业务
 */
public class SessionHolder {
    /**
     *
     */
    private static final Map<String, SyncObj> syncObj = new HashMap<>();

    /**
     * uuid和各客户端channel的映射 用于根据uuid推送消息
     * 每个客户端使用Map的key做映射 避免并发
     */
    private static final Map<Integer, ConcurrentHashMap<String, SessionModel>> session = new HashMap<>();

    //初始化时 将用到的每个客户端平台注册 定义在Constant.clientType中
    static {
        for (Integer type : Constant.clientType)
            session.put(type, new ConcurrentHashMap<>());
    }

    public static ConcurrentHashMap<String, SessionModel> getSession(int deviceType) {
        int type = Constant.mapDevice(deviceType);
        return session.get(type);
    }

    public static boolean checkSession(String uuid) {
        for (Integer type : Constant.clientType)
            if (session.get(type).contains(uuid))
                return true;
        return false;
    }


    /**
     * channel和uuid的映射 用于退出登录
     */
    public static final ConcurrentHashMap<Channel, SessionModel> sessionChannelMap = new ConcurrentHashMap<>();

    /**
     * 先缓存消息 收到客户端确认收到消息 才移除 保证送达
     */
    public static final ConcurrentHashMap<String, ReceiptModel> receiptMsg = new ConcurrentHashMap<>();

    //将channel和uuid做本地映射
    public static void login(Channel channel, BaseMsgModel msgModel) {
        SessionModel sessionModel = new SessionModel();
        sessionModel.channel = channel;
        sessionModel.clientToken = ((CmdMsgModel) msgModel).fromToken;
        sessionModel.deviceType = ((CmdMsgModel) msgModel).deviceType;
        sessionModel.uuid = msgModel.from;

        getSession(msgModel.deviceType).put(msgModel.from, sessionModel);
        sessionChannelMap.put(channel, sessionModel);
    }

    public static void logout(CmdMsgModel cmdMsg) {
        SessionModel sessionModel = getSession(cmdMsg.deviceType).remove(cmdMsg.from);
        if (sessionModel != null)
            sessionChannelMap.remove(sessionModel.channel);
    }

    public static SessionModel logout(Channel channel) {
        SessionModel sessionModel = sessionChannelMap.remove(channel);
        if (sessionModel != null)
            getSession(sessionModel.deviceType).remove(sessionModel.uuid);
        return sessionModel;
    }

    /**
     * 本服务器推送消息
     *
     * @param self 是否给自己的其他客户端推送
     */
    public static void sendMsg(BaseMsgModel msgModel, boolean self) {
        List<SessionModel> sessionModelList = new ArrayList<>();
        for (ConcurrentHashMap<String, SessionModel> map : session.values()) {
            SessionModel smTo = map.get(msgModel.to);
            if (smTo != null)
                sessionModelList.add(smTo);
            if (self) {
                SessionModel smFrom = map.get(msgModel.from);
                //不需要发给发送着
                if (smFrom != null && smFrom.clientToken != msgModel.fromToken)
                    sessionModelList.add(smFrom);
            }
        }

        for (SessionModel sm : sessionModelList) {
            sm.channel.writeAndFlush(msgModel);

            ReceiptModel recModel = new ReceiptModel();
            recModel.channel = new WeakReference<>(sm.channel);
            msgModel.sendTime = System.currentTimeMillis();
            recModel.msgModel = msgModel;
            //加入回执缓存
            SessionHolder.receiptMsg.put(recModel.token(), recModel);
        }
    }

    public static class SyncObj {
        public long createTime;

        public SyncObj(long createTime) {
            this.createTime = createTime;
        }

    }

    public static synchronized SyncObj getSync(String uuid) {
        SyncObj obj = syncObj.get(uuid);
        if (obj == null) {
            obj = new SyncObj(System.currentTimeMillis());
            syncObj.put(uuid, obj);
        }
        return obj;
    }
}
