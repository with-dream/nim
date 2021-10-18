package com.example.server.netty;

import com.example.server.ApplicationRunnerImpl;
import com.example.server.entity.GroupMemberModel;
import com.example.server.entity.RecCacheEntity;
import com.example.server.service.UserService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import netty.entity.MsgLevel;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import org.redisson.api.RSet;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;
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

    private static SendHolder that;

    @PostConstruct
    public void init() {
        that = this;
    }

    @Resource
    public RedissonClient redisson;

    @Resource
    public UserService userService;

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

    /**
     * 服务器客户端的名称-channel映射 用于服务器间传消息
     */
    public Map<String, Channel> transferMap = new HashMap<>();

    /**
     * 1 如果所在客户端平台已登录 则强制退出
     * 2 将uuid和channel做映射 用于消息推送
     * 3 将uuid和mq做映射 用于将消息路由到本服务器中转到channel所在的服务器
     */
    public void login(Channel channel, NimMsg msg) {
        SessionRedisModel sessionModel = new SessionRedisModel();
        sessionModel.clientToken = msg.fromToken;
        sessionModel.deviceType = msg.deviceType;
        sessionModel.uuid = msg.from;
        sessionModel.queueName = ApplicationRunnerImpl.MQ_NAME;

        RSetMultimap<String, SessionRedisModel> multimap = that.redisson.getSetMultimap(UUID_MQ_MAP);
        RSet<SessionRedisModel> sessionModels = multimap.get(msg.from);
        //重复登录
        if (sessionModels.contains(sessionModel))
            return;

        //TODO 验证强制退出操作

        boolean res = multimap.put(msg.from, sessionModel);
        if (res) {
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
            //重复登录
            if (set.contains(sm)) return;

            channel.attr(UUID_CHANNEL_MAP).set(msg.from);
        }
    }

    public void logout(Channel channel) {
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
        else sendMsgLocal(msg);
    }

    private void sendMsgServiceSingle(NimMsg msg) {
        Set<String> uuidSet = new HashSet<>();
        switch (msg.msgType) {
            case MsgType.TYPE_GROUP:
            case MsgType.TYPE_CMD_GROUP:
                List<GroupMemberModel> memList = userService.getGroupMembers(msg.getGroupId());
                for (GroupMemberModel gmm : memList)
                    uuidSet.add(gmm.uuid);
                msg.getMsgMap().put(MsgType.KEY_UNIFY_SERVICE_UUID_Set, uuidSet);
                sendMsgLocal(msg);
                break;
            case MsgType.TYPE_CMD:
            case MsgType.TYPE_MSG:
            case MsgType.TYPE_RECEIPT:
                if (NullUtil.isTrue(msg.getMsgMap().get(MsgType.KEY_UNIFY_CLIENT_SEND_SELF)))
                    uuidSet.add(msg.from);
                uuidSet.add(msg.to);
                msg.getMsgMap().put(MsgType.KEY_UNIFY_SERVICE_UUID_Set, uuidSet);
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
                if (NullUtil.isTrue(msg.getMsgMap().get(MsgType.KEY_UNIFY_CLIENT_SEND_SELF)))
                    set.addAll(multimap.get(msg.from));
                set.addAll(multimap.get(msg.to));
                sendMsgServiceNormal(msg, set);
                break;
            case MsgType.TYPE_ROOT:
                sendMsgServiceRoot(msg);
                break;
        }
    }

    private void sendMsgServiceRoot(NimMsg msg) {
        for (Map.Entry<String, Channel> entry : transferMap.entrySet()) {
            if (entry.getKey().equals(ApplicationRunnerImpl.MQ_NAME)) {
                sendMsgLocal(msg);
            } else {
                ChannelFuture future = entry.getValue().writeAndFlush(entry.getValue());
                future.addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> f) {
                        if (f.isSuccess()) {

                        } else {
                            //TODO 发送失败
                            L.e("transferMap转发失败");
                        }
                        future.removeListener(this);
                    }
                });
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
                NimMsg tmpMsg = msg.copy();
                uuidSet = new HashSet<>();
                tmpMsg.getMsgMap().put(MsgType.KEY_UNIFY_SERVICE_UUID_Set, uuidSet);
                sendTmpMsg.put(srm.queueName, tmpMsg);
            } else {
                NimMsg tmpMsg = sendTmpMsg.get(srm.queueName);
                uuidSet = (Set<String>) tmpMsg.getMsgMap().get(MsgType.KEY_UNIFY_SERVICE_UUID_Set);
            }

            uuidSet.add(srm.uuid);
        }

        for (Map.Entry<String, NimMsg> entry : sendTmpMsg.entrySet()) {
            if (entry.getKey().equals(ApplicationRunnerImpl.MQ_NAME)) {
                sendMsgLocal(msg);
            } else {
                ChannelFuture future = transferMap.get(entry.getKey()).writeAndFlush(entry.getValue());
                future.addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> f) throws Exception {
                        if (f.isSuccess()) {

                        } else {
                            //TODO 发送失败
                            L.e("transferMap转发失败");
                        }
                        future.removeListener(this);
                    }
                });
            }
        }
    }

    /**
     * 将消息推送给连接此服务器的客户端
     */
    private void sendMsgLocal(NimMsg msg) {
        Set<String> toList = new HashSet<>();

        switch (msg.msgType) {
            case MsgType.TYPE_GROUP:
            case MsgType.TYPE_CMD_GROUP:
            case MsgType.TYPE_CMD:
            case MsgType.TYPE_MSG:
            case MsgType.TYPE_RECEIPT:
                toList.addAll(NullUtil.isSet(msg.getMsgMap().get(MsgType.KEY_UNIFY_SERVICE_UUID_Set)));
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
        NimMsg tmpMsg = msg.copy();
        String token = tmpMsg.newTokenService();
        ChannelFuture future = sm.channel.writeAndFlush(tmpMsg);

        if (msg.level != MsgLevel.LEVEL_LOW) {
            RecCacheEntity rce = new RecCacheEntity(0, new WeakReference<>(sm), msg);
            rce.token = token;
            rce.updateTime();
            receiptMap.put(token, rce);
        }

        //不重要的消息 如果成功不操作 如果失败则重发 只有失败才操作
        if (msg.level == MsgLevel.LEVEL_LOW) {
            future.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> f) {
                    if (!f.isSuccess()) {
                        RecCacheEntity rce = new RecCacheEntity(0, new WeakReference<>(sm), msg);
                        rce.token = token;
                        rce.updateTime();
                        receiptMap.put(token, rce);
                    }

                    future.removeListener(this);
                }
            });
        }
    }

    static class ReceiptThread extends Thread {
        private boolean run = true;
        public PriorityBlockingQueue<RecCacheEntity> receiptQueue = new PriorityBlockingQueue<>();

        @Override
        public void run() {
            super.run();
            while (run) {
                if (receiptQueue.isEmpty())
                    LockSupport.park();

                RecCacheEntity recF = receiptQueue.peek();
                if (recF.unpackTime - System.currentTimeMillis() <= 50) {
                    Iterator<RecCacheEntity> it = receiptQueue.iterator();
                    while (it.hasNext()) {
                        RecCacheEntity rce = it.next();
                        if (rce.isTimeout()) {
                            //TODO 超时处理
                            receiptMap.remove(rce.token);
                            it.remove();
                        } else if (rce.sm.get() != null
                                && rce.unpackTime - System.currentTimeMillis() <= 50) {
                            rce.sm.get().channel.writeAndFlush(rce.msg);
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
    }
}
