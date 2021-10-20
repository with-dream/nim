package com.example.server.netty;

import com.example.server.ApplicationRunnerImpl;
import com.example.server.entity.GroupMemberModel;
import com.example.server.entity.RecCacheEntity;
import com.example.server.netty.entity.SessionModel;
import com.example.server.netty.entity.SessionRedisModel;
import com.example.server.service.UserService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSet;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import utils.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.LockSupport;

/**
 * 整个服务器的消息发送
 */
@Component
public class SendHolder {
    public static boolean COLONY = true;
    public static final String UUID_MQ_MAP = "mq_map";
    public static final AttributeKey<String> UUID_CHANNEL_MAP = AttributeKey.newInstance("key_uuid_channel_map");
    private static final boolean LOGIN_DEBUG = true;

    private static SendHolder that;

    public SendHolder() {
        L.e("构造方法==>SendHolder");
        if (!receiptThread.isAlive())
            receiptThread.start();
    }

    @PostConstruct
    public void init() {
        that = this;
    }

    @Resource
    public RedissonClient redisson;

    @Resource
    public UserService userService;

    //TODO 需要添加消息确认
    @Resource
    public AmqpTemplate rabbit;

    /**
     * 缓存uuid和客户端的映射
     * 同一个uuid会对应多个客户端
     */
    public ConcurrentMap<String, Set<SessionModel>> session = new ConcurrentHashMap<>();

    /**
     * 已发送的消息缓存 用于消息重发
     * key为NimMsg的临时token
     */
    public static Map<String, RecCacheEntity> receiptMap = new HashMap<>();
    private ReceiptThread receiptThread = new ReceiptThread();

    /**
     * 服务器客户端的名称-channel映射 用于服务器间传消息
     */
    public Map<String, String> transferMap = new HashMap<>();

    /**
     * 1 如果所在客户端平台已登录 则强制退出
     * 2 将uuid和channel做映射 用于消息推送
     * 3 将uuid和mq做映射 用于将消息路由到本服务器中转到channel所在的服务器
     */
    public int login(Channel channel, NimMsg msg) {
        if (LOGIN_DEBUG) {
            L.p("login msg==>" + msg);
            L.p("login session==>" + session);
        }
        int ret = 0;
        SessionRedisModel sessionModel = new SessionRedisModel();
        sessionModel.clientToken = msg.fromToken;
        sessionModel.deviceType = msg.deviceType;
        sessionModel.uuid = msg.from;
        sessionModel.queueName = ApplicationRunnerImpl.MQ_NAME;

        RSetMultimap<String, SessionRedisModel> multimap = that.redisson.getSetMultimap(UUID_MQ_MAP);
        RSet<SessionRedisModel> sessionModels = multimap.get(msg.from);
        boolean redisCached = sessionModels.contains(sessionModel);
        if (LOGIN_DEBUG) {
            L.p("login redisCached==>" + redisCached);
        }
        //重复登录
        if (redisCached) {
            boolean add = true;
            Set<SessionModel> set = session.get(msg.from);
            if (!CollectionUtils.isEmpty(set))
                for (SessionModel sm : set) {
                    if (sm.redisModel.equals(sessionModel)) {
                        add = false;
                        break;
                    }
                }
            if (LOGIN_DEBUG) {
                L.p("login add local session==>" + add);
            }
            if (add) {
                ret = addSession(channel, msg, sessionModel);
                if (LOGIN_DEBUG)
                    L.p("login add local session ret==>" + ret);
            }
            return ret;
        }
        //TODO 验证强制退出操作

        boolean res = multimap.put(msg.from, sessionModel);
        if (res) {
            ret = addSession(channel, msg, sessionModel);
        }
        if (LOGIN_DEBUG) {
            L.p("login session ret==>" + session);
        }
        return ret;
    }

    private int addSession(Channel channel, NimMsg msg, SessionRedisModel sessionModel) {
        SessionModel sm = new SessionModel();
        sm.redisModel = sessionModel;
        sm.channel = channel;
        Set<SessionModel> set = session.get(msg.from);
        if (set == null) {
            synchronized (session) {
                if (set == null) {
                    set = Collections.synchronizedSet(new HashSet<>());
                    session.put(msg.from, set);
                }
            }
        }
        channel.attr(UUID_CHANNEL_MAP).set(msg.from);
        //重复登录
        if (set.contains(sm))
            return -1;
        else
            set.add(sm);

        return 0;
    }

    public int logout(Channel channel) {
        String uuid = channel.attr(UUID_CHANNEL_MAP).get();

        Set<SessionModel> localSet = session.get(uuid);
        SessionModel cacheSM = null;
        for (SessionModel sm : localSet) {
            if (sm.channel == channel) {
                cacheSM = sm;
                break;
            }
        }
        localSet.remove(cacheSM);

        RSetMultimap<String, SessionRedisModel> multimap = that.redisson.getSetMultimap(UUID_MQ_MAP);
        RSet<SessionRedisModel> sessionModels = multimap.get(uuid);
        if (CollectionUtils.isEmpty(sessionModels)) {
            throw new RuntimeException("uuid丢失redis缓存");
        }
        boolean res = sessionModels.remove(cacheSM.redisModel);
        if (!res) {
            L.e("==>sessionModels.remove fail");
        }

        channel.close();

        return 0;
    }

    //关闭服务器操作
    public void closeMQ() {
        for (Iterator<Map.Entry<String, Set<SessionModel>>> it = session.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Set<SessionModel>> set = it.next();

            for (Iterator<SessionModel> smIt = set.getValue().iterator(); smIt.hasNext(); ) {
                SessionModel sm = smIt.next();
                if (ApplicationRunnerImpl.MQ_NAME.equals(sm.redisModel.queueName)) {
                    smIt.remove();
                    //删除redis中的session
                    RSetMultimap<String, SessionRedisModel> multimap = that.redisson.getSetMultimap(UUID_MQ_MAP);
                    RSet<SessionRedisModel> sessionModels = multimap.get(sm.redisModel.uuid);
                    if (CollectionUtils.isEmpty(sessionModels)) {
                        throw new RuntimeException("uuid丢失redis缓存");
                    }
                    boolean res = sessionModels.remove(sm.redisModel);
                    if (!res) {
                        L.e("==>sessionModels.remove fail");
                    }

                    sm.channel.close();
                }
            }
            if (set.getValue().isEmpty())
                it.remove();
        }
    }

    /**
     * 推送消息
     */
    public void sendMsg(NimMsg msg) {
        if (COLONY)
            sendMsgServiceColony(msg);
        else sendMsgServiceSingle(msg);
    }

    private void sendMsgServiceSingle(NimMsg msg) {
        Set<String> uuidSet = new HashSet<>();
        switch (msg.msgType) {
            case MsgType.TYPE_GROUP:
            case MsgType.TYPE_CMD_GROUP:
                List<GroupMemberModel> memList = userService.getGroupMembers(msg.getGroupId());
                for (GroupMemberModel gmm : memList)
                    uuidSet.add(gmm.uuid);
                msg.msgMap().put(MsgType.KEY_UNIFY_SERVICE_UUID_SET, uuidSet);
                sendMsgLocal(msg);
                break;
            case MsgType.TYPE_CMD:
            case MsgType.TYPE_MSG:
            case MsgType.TYPE_RECEIPT:
                if (NullUtil.isTrue(msg.msgMap().get(MsgType.KEY_UNIFY_CLIENT_SEND_SELF)))
                    uuidSet.add(msg.from);
                uuidSet.add(msg.to);
                msg.msgMap().put(MsgType.KEY_UNIFY_SERVICE_UUID_SET, uuidSet);
                sendMsgLocal(msg);
                break;
            case MsgType.TYPE_ROOT:
                sendMsgRoot(msg);
                break;
        }
    }

    //TODO 发送到其他服务器中转客户端 需要缓存消息 用于重发
    private void sendMsgServiceColony(NimMsg msg) {
        Set<SessionRedisModel> set = new HashSet<>();
        RSetMultimap<String, SessionRedisModel> multimap = that.redisson.getSetMultimap(UUID_MQ_MAP);

        switch (msg.msgType) {
            case MsgType.TYPE_GROUP:
            case MsgType.TYPE_CMD_GROUP:
                List<GroupMemberModel> memList = userService.getGroupMembers(msg.getGroupId());
                for (GroupMemberModel gmm : memList) {
                    set.addAll(multimap.get(gmm.uuid));
                }
                sendMsgServiceNormal(msg, set);
                break;
            case MsgType.TYPE_CMD:
            case MsgType.TYPE_MSG:
            case MsgType.TYPE_RECEIPT:
                if (NullUtil.isTrue(msg.msgMap().get(MsgType.KEY_UNIFY_CLIENT_SEND_SELF)))
                    set.addAll(multimap.get(msg.from));
                set.addAll(multimap.get(msg.to));
                L.p("redisSession==>" + set);
                sendMsgServiceNormal(msg, set);
                break;
            case MsgType.TYPE_ROOT:
                sendMsgServiceRoot(msg);
                break;
        }
    }

    private void sendMsgServiceRoot(NimMsg msg) {
        for (Map.Entry<String, String> entry : transferMap.entrySet()) {
            if (entry.getKey().equals(ApplicationRunnerImpl.MQ_NAME)) {
                sendMsgLocal(msg);
            } else {
                rabbit.convertAndSend(entry.getValue(), msg);
            }
        }
    }

    private void sendMsgServiceNormal(NimMsg msg, Set<SessionRedisModel> set) {
        // queueName用于标识一个服务器
        // 将所有queueName相同的消息合并为一个 使用KEY_UNIFY_SERVICE_GROUP_UUID_LIST保存所有的目标uuid
        Map<String, NimMsg> sendTmpMsg = new HashMap<>();
        for (SessionRedisModel srm : set) {
            Set<String> uuidSet = null;
            if (!sendTmpMsg.containsKey(srm.queueName)) {
                NimMsg tmpMsg = msg.copyDeep();
                uuidSet = new HashSet<>();
                tmpMsg.msgMap().put(MsgType.KEY_UNIFY_SERVICE_UUID_SET, uuidSet);
                sendTmpMsg.put(srm.queueName, tmpMsg);
            } else {
                NimMsg tmpMsg = sendTmpMsg.get(srm.queueName);
                uuidSet = (Set<String>) tmpMsg.msgMap().get(MsgType.KEY_UNIFY_SERVICE_UUID_SET);
            }

            uuidSet.add(srm.uuid);
        }

        for (Map.Entry<String, NimMsg> entry : sendTmpMsg.entrySet()) {
            if (entry.getKey().equals(ApplicationRunnerImpl.MQ_NAME)) {
                sendMsgLocal(entry.getValue());
            } else {
                rabbit.convertAndSend(transferMap.get(entry.getKey()), entry.getValue());
            }
        }
    }

    /**
     * 将消息推送给连接此服务器的客户端
     */
    public void sendMsgLocal(NimMsg msg) {
        Set<String> toList = new HashSet<>();

        switch (msg.msgType) {
            case MsgType.TYPE_GROUP:
            case MsgType.TYPE_CMD_GROUP:
            case MsgType.TYPE_CMD:
            case MsgType.TYPE_MSG:
            case MsgType.TYPE_RECEIPT:
            case MsgType.TYPE_PACK:
                toList.addAll(NullUtil.isSet(msg.msgMap().get(MsgType.KEY_UNIFY_SERVICE_UUID_SET)));
                sendMsgNormal(msg, toList);
                break;
            case MsgType.TYPE_ROOT:
                sendMsgRoot(msg);
                break;
        }
    }

    private void sendMsgRoot(NimMsg msg) {
        for (Set<SessionModel> set : session.values())
            for (SessionModel sm : set) {
                //发送者所在客户端 不需要发送给自己
                if (sm.redisModel.uuid.equals(msg.from) && sm.redisModel.clientToken == msg.fromToken)
                    continue;
                sendMsgReal(msg, sm);
            }
    }

    private void sendMsgNormal(NimMsg msg, Set<String> toList) {
        Set<SessionModel> smSet = new HashSet<>();
        for (String to : toList)
            smSet.addAll(session.get(to));
        if (CollectionUtils.isEmpty(smSet)) {
            L.e("sendMsgLocal 查找uuid错误");
        }

        for (SessionModel sm : smSet) {
            //发送者所在客户端 不需要发送给自己
            if (sm.redisModel.uuid.equals(msg.from) && sm.redisModel.clientToken == msg.fromToken)
                continue;
            sendMsgReal(msg, sm);
        }
    }

    private void sendMsgReal(NimMsg msg, SessionModel sm) {
        NimMsg tmpMsg = msg.copyDeep();
        String token = tmpMsg.newTokenService(NimMsg.isRecMsg(msg));
        ChannelFuture future = sm.channel.writeAndFlush(tmpMsg);

        if (NimMsg.isRecMsg(msg)) {
            RecCacheEntity rce = new RecCacheEntity(0, new WeakReference<>(sm), msg);
            rce.token = token;
            rce.updateTime();
            putRecMsg(token, rce);
        }

        //不重要的消息 如果成功不操作 如果失败则重发 只有失败才操作
        if (!NimMsg.isRecMsg(msg)) {
            future.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> f) {
                    if (!f.isSuccess()) {
                        RecCacheEntity rce = new RecCacheEntity(0, new WeakReference<>(sm), msg);
                        rce.token = token;
                        rce.updateTime();
                        putRecMsg(token, rce);
                    }

                    future.removeListener(this);
                }
            });
        }
    }

    public void checkRecMsg(NimMsg msg) {
        if (msg.msgType != MsgType.TYPE_RECEIPT)
            return;

        String token = NullUtil.isStr(msg.recMap().get(MsgType.KEY_UNIFY_SERVICE_MSG_TOKEN));
        if (StringUtils.isEmpty(token)) return;

        removeRecMsg(token);
    }

    public void recThreadStart() {
        if (receiptThread.isAlive()) return;
        receiptThread.start();
    }

    public void recThreadExit() {
        receiptThread.exit();
    }

    public void putRecMsg(String token, RecCacheEntity value) {
        receiptThread.addCache(value);
        receiptMap.put(token, value);
    }

    public boolean removeRecMsg(String token) {
        RecCacheEntity rce = receiptMap.get(token);
        if (rce == null) return false;
        return receiptThread.removeCache(rce);
    }

    static class ReceiptThread extends Thread {
        private boolean run = true;
        public Queue<RecCacheEntity> receiptQueue = new PriorityBlockingQueue<>();

        @Override
        public void run() {
            super.run();
            while (run) {
                if (receiptQueue.isEmpty())
                    LockSupport.park();

                RecCacheEntity recF = receiptQueue.peek();
                if (recF == null) continue;

                if (recF.unpackTime - System.currentTimeMillis() <= 50) {
                    Iterator<RecCacheEntity> it = receiptQueue.iterator();
                    while (it.hasNext()) {
                        RecCacheEntity rce = it.next();
                        if (rce == null) continue;
                        if (rce.isTimeout()) {
                            //TODO 超时处理
                            receiptMap.remove(rce.token);
                            it.remove();
                        } else if (rce.sm.get() != null
                                && rce.unpackTime - System.currentTimeMillis() <= 50) {
                            ChannelFuture future = rce.sm.get().channel.writeAndFlush(rce.msg);
                            //如果不是重要的消息 则发送完成即删除
                            if (!NimMsg.isRecMsg(rce.msg))
                                future.addListener(new GenericFutureListener<Future<? super Void>>() {
                                    @Override
                                    public void operationComplete(Future<? super Void> f) throws Exception {
                                        if (f.isSuccess()) {
                                            receiptMap.remove(rce.token);
                                            it.remove();
                                        }

                                        future.removeListener(this);
                                    }
                                });
                            rce.updateTime();
                        } else
                            break;
                    }
                } else
                    LockSupport.parkUntil(recF.unpackTime - System.currentTimeMillis());
            }
        }

        public void exit() {
            run = false;
        }

        public void addCache(RecCacheEntity cacheEntity) {
            receiptQueue.add(cacheEntity);
            if (receiptQueue.size() > 1)
                if (receiptQueue.peek().unpackTime < cacheEntity.unpackTime)
                    LockSupport.unpark(this);
        }

        public boolean removeCache(RecCacheEntity cacheEntity) {
            return receiptQueue.remove(cacheEntity);
        }
    }
}
