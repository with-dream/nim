package com.example.server.netty;

import com.example.server.utils.Const;
import entity.Entity;
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
 *
 * 由客户端消息标识控制严苛级别
 *
 * 消息分级别
 * 1 当前端将数据发送出去即完成 只有异常 才回执
 * 2 消息发送后 在客户端进行缓存 目标客户端收到消息回执 如果未收到回执 则重发
 * 3 消息从客户端->服务器 服务器->目标客户端 每级都有消息回执 确保发送成功
 *
 * 1 客户端发送 客户端根据msgId缓存一份
 * 2 服务器接收到客户端的消息 查找所有目标客户端的uuid 然后分发 分发前缓存一份 需要记录所有需要发送的客户端
*       2.1 如果是普通模式
 *       2.1 如果每个客户端不在同一个服务器 则各自发送
 *       2.2 如果多个客户端在同一个服务器
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
        //查找需要发送的客户端信息
        List<SessionModel> sessionModelList = new ArrayList<>();
        for (ConcurrentHashMap<String, SessionModel> map : session.values()) {
            SessionModel smTo = map.get(msg.to);
            if (smTo != null)
                sessionModelList.add(smTo);
            if (self) {
                SessionModel smFrom = map.get(msg.from);
                //不需要发给发送者
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

    public static class SendMsgCache {
        public NimMsg msg;
        public int deviceType;
        public long sendTime;
        public int tryCount;
        public WeakReference<Channel> channel;
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
