package com.example.server.netty;

import com.example.server.utils.Const;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import utils.Constant;
import utils.L;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 当前服务器的推送及基础业务
 */
public class SessionHolder {
    public static final AttributeKey<SessionModel> UUID_CHANNEL = AttributeKey.valueOf("uuid_channel_map");

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
     * 先缓存消息 收到客户端确认收到消息 才移除 保证送达
     */
    public static final ConcurrentHashMap<String, NimMsg> receiptMsg = new ConcurrentHashMap<>();

    //将channel和uuid做本地映射
    public static void login(Channel channel, NimMsg msg) {
        SessionModel sessionModel = new SessionModel();
        sessionModel.channel = channel;
        sessionModel.clientToken = msg.fromToken;
        sessionModel.deviceType = msg.deviceType;
        sessionModel.uuid = msg.from;

        getSession(msg.deviceType).put(msg.from, sessionModel);
        channel.attr(UUID_CHANNEL).set(sessionModel);

        if (Const.DEBUG) {
            L.p("login getSession==>" + getSession(msg.deviceType));
        }
    }

    public static SessionModel logout(Channel channel) {
        SessionModel sessionModel = channel.attr(UUID_CHANNEL).get();
        if (sessionModel != null)
            getSession(sessionModel.deviceType).remove(sessionModel.uuid);
        return sessionModel;
    }

    /**
     * 本服务器推送消息
     *
     * @param self 是否给自己的其他客户端推送
     */
    public static void sendMsg(NimMsg msg, boolean self) {
        List<SessionModel> sessionModelList = new ArrayList<>();
        for (ConcurrentHashMap<String, SessionModel> map : session.values()) {
            SessionModel smTo = map.get(msg.to);
            if (smTo != null)
                sessionModelList.add(smTo);
            if (self) {
                SessionModel smFrom = map.get(msg.from);
                //不需要发给发送着
                if (smFrom != null && smFrom.clientToken != msg.fromToken)
                    sessionModelList.add(smFrom);
            }
        }
        if (Const.DEBUG) {
            L.p("sendMsg local==>" + sessionModelList);
        }
        for (SessionModel sm : sessionModelList) {
            sm.channel.writeAndFlush(msg);
            msg.recPut(MsgType.KEY_UNIFY_SERVICE_SEND_TIME, System.currentTimeMillis());
            msg.recPut(MsgType.KEY_UNIFY_SERVICE_SEND_CHANNEL, new WeakReference<>(sm.channel));
            //加入回执缓存
            receiptMsg.put(msg.tokenService(), msg);
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
