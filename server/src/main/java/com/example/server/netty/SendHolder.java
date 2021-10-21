package com.example.server.netty;

import com.alibaba.fastjson.JSON;
import com.example.server.ApplicationRunnerImpl;
import com.example.server.netty.entity.RecCacheEntity;
import com.example.server.netty.entity.SessionEntity;
import com.example.server.netty.entity.SessionRedisEntity;
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
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
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
    public static final String MQ_SET = "mq_set";
    public static final AttributeKey<String> UUID_CHANNEL_MAP = AttributeKey.newInstance("key_uuid_channel_map");
    private static final boolean LOGIN_DEBUG = true;
    private GroupMember groupMember;

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

    //TODO 需要添加消息确认
    @Resource
    public AmqpTemplate rabbit;

    /**
     * 缓存uuid和客户端的映射
     * 同一个uuid会对应多个客户端
     */
    public ConcurrentMap<String, Set<SessionEntity>> session = new ConcurrentHashMap<>();

    /**
     * 已发送的消息缓存 用于消息重发
     * key为NimMsg的临时token
     */
    public static Map<String, RecCacheEntity> receiptMap = new HashMap<>();
    private ReceiptThread receiptThread = new ReceiptThread();

    public void setGroupMember(GroupMember groupMember) {
        this.groupMember = groupMember;
    }

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
        SessionRedisEntity sessionEntity = new SessionRedisEntity();
        sessionEntity.clientToken = msg.fromToken;
        sessionEntity.deviceType = msg.deviceType;
        sessionEntity.uuid = msg.from;
        sessionEntity.queueName = ApplicationRunnerImpl.MQ_NAME;

        RSetMultimap<String, SessionRedisEntity> multimap = that.redisson.getSetMultimap(UUID_MQ_MAP);
        RSet<SessionRedisEntity> sessionEntitys = multimap.get(msg.from);
        boolean redisCached = sessionEntitys.contains(sessionEntity);
        if (LOGIN_DEBUG) {
            L.p("login redisCached==>" + redisCached);
        }
        //重复登录
        if (redisCached) {
            boolean add = true;
            Set<SessionEntity> set = session.get(msg.from);
            if (!CollectionUtils.isEmpty(set))
                for (SessionEntity sm : set) {
                    if (sm.redisEntity.equals(sessionEntity)) {
                        add = false;
                        break;
                    }
                }
            if (LOGIN_DEBUG) {
                L.p("login add local session==>" + add);
            }
            if (add) {
                ret = addSession(channel, msg, sessionEntity);
                if (LOGIN_DEBUG)
                    L.p("login add local session ret==>" + ret);
            }
            return ret;
        }
        //TODO 验证强制退出操作

        boolean res = multimap.put(msg.from, sessionEntity);
        if (res) {
            ret = addSession(channel, msg, sessionEntity);
        }
        if (LOGIN_DEBUG) {
            L.p("login session ret==>" + session);
        }
        return ret;
    }

    private int addSession(Channel channel, NimMsg msg, SessionRedisEntity sessionEntity) {
        SessionEntity sm = new SessionEntity();
        sm.redisEntity = sessionEntity;
        sm.channel = channel;
        Set<SessionEntity> set = session.get(msg.from);
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

        Set<SessionEntity> localSet = session.get(uuid);
        SessionEntity cacheSM = null;
        for (SessionEntity sm : localSet) {
            if (sm.channel == channel) {
                cacheSM = sm;
                break;
            }
        }
        localSet.remove(cacheSM);

        RSetMultimap<String, SessionRedisEntity> multimap = that.redisson.getSetMultimap(UUID_MQ_MAP);
        RSet<SessionRedisEntity> sessionEntitys = multimap.get(uuid);
        if (CollectionUtils.isEmpty(sessionEntitys)) {
            throw new RuntimeException("uuid丢失redis缓存");
        }
        boolean res = sessionEntitys.remove(cacheSM.redisEntity);
        if (!res) {
            L.e("==>sessionEntitys.remove fail");
        }

        channel.close();

        return 0;
    }

    //关闭服务器操作
    public void closeMQ() {
        for (Iterator<Map.Entry<String, Set<SessionEntity>>> it = session.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Set<SessionEntity>> set = it.next();

            for (Iterator<SessionEntity> smIt = set.getValue().iterator(); smIt.hasNext(); ) {
                SessionEntity sm = smIt.next();
                if (ApplicationRunnerImpl.MQ_NAME.equals(sm.redisEntity.queueName)) {
                    smIt.remove();
                    //删除redis中的session
                    RSetMultimap<String, SessionRedisEntity> multimap = that.redisson.getSetMultimap(UUID_MQ_MAP);
                    RSet<SessionRedisEntity> sessionEntitys = multimap.get(sm.redisEntity.uuid);
                    if (CollectionUtils.isEmpty(sessionEntitys)) {
                        throw new RuntimeException("uuid丢失redis缓存");
                    }
                    boolean res = sessionEntitys.remove(sm.redisEntity);
                    if (!res) {
                        L.e("==>sessionEntitys.remove fail");
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
                if (this.groupMember != null)
                    uuidSet.addAll(this.groupMember.memberUuid(msg.getGroupId()));
                msg.msgMap().put(MsgType.KEY_UNIFY_SERVICE_UUID_LIST, new ArrayList<>(uuidSet));
                sendMsgLocal(msg);
                break;
            case MsgType.TYPE_CMD:
            case MsgType.TYPE_MSG:
            case MsgType.TYPE_RECEIPT:
                if (NullUtil.isTrue(msg.msgMap().get(MsgType.KEY_UNIFY_CLIENT_SEND_SELF)))
                    uuidSet.add(msg.from);
                uuidSet.add(msg.to);
                msg.msgMap().put(MsgType.KEY_UNIFY_SERVICE_UUID_LIST, new ArrayList<>(uuidSet));
                sendMsgLocal(msg);
                break;
            case MsgType.TYPE_ROOT:
                sendMsgRoot(msg);
                break;
        }
    }

    //TODO 发送到其他服务器中转客户端 需要缓存消息 用于重发
    private void sendMsgServiceColony(NimMsg msg) {
        Set<SessionRedisEntity> set = new HashSet<>();
        RSetMultimap<String, SessionRedisEntity> multimap = that.redisson.getSetMultimap(UUID_MQ_MAP);

        switch (msg.msgType) {
            case MsgType.TYPE_GROUP:
            case MsgType.TYPE_CMD_GROUP:
                if (this.groupMember != null)
                    for (String uuid : this.groupMember.memberUuid(msg.getGroupId()))
                        set.addAll(multimap.get(uuid));
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
        RSet<String> mqSet = redisson.getSet(MQ_SET);
        for (String mq : mqSet) {
            if (mq.equals(ApplicationRunnerImpl.MQ_NAME)) {
                sendMsgLocal(msg);
            } else {
                MessageProperties messageProperties = new MessageProperties();
                Message message = new Message(JSON.toJSONString(msg).getBytes(), messageProperties);
                rabbit.convertAndSend(mq, message);
            }
        }
    }

    private void sendMsgServiceNormal(NimMsg msg, Set<SessionRedisEntity> set) {
        // queueName用于标识一个服务器
        // 将所有queueName相同的消息合并为一个 使用KEY_UNIFY_SERVICE_GROUP_UUID_LIST保存所有的目标uuid
        Map<String, NimMsg> sendTmpMsg = new HashMap<>();
        for (SessionRedisEntity srm : set) {
            List<String> uuidList = null;
            if (!sendTmpMsg.containsKey(srm.queueName)) {
                NimMsg tmpMsg = msg.copyDeep();
                uuidList = new ArrayList<>();
                tmpMsg.msgMap().put(MsgType.KEY_UNIFY_SERVICE_UUID_LIST, uuidList);
                sendTmpMsg.put(srm.queueName, tmpMsg);
            } else {
                NimMsg tmpMsg = sendTmpMsg.get(srm.queueName);
                uuidList = (List<String>) tmpMsg.msgMap().get(MsgType.KEY_UNIFY_SERVICE_UUID_LIST);
            }
            if (!uuidList.contains(srm.uuid))
                uuidList.add(srm.uuid);
        }

        for (Map.Entry<String, NimMsg> entry : sendTmpMsg.entrySet()) {
            if (entry.getKey().equals(ApplicationRunnerImpl.MQ_NAME)) {
                sendMsgLocal(entry.getValue());
            } else {
                MessageProperties messageProperties = new MessageProperties();
                Message message = new Message(JSON.toJSONString(entry.getValue()).getBytes(), messageProperties);
                rabbit.convertAndSend(entry.getKey(), message);
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
                toList.addAll(NullUtil.isList(msg.msgMap().get(MsgType.KEY_UNIFY_SERVICE_UUID_LIST)));
                sendMsgNormal(msg, toList);
                break;
            case MsgType.TYPE_ROOT:
                sendMsgRoot(msg);
                break;
        }
    }

    private void sendMsgRoot(NimMsg msg) {
        for (Set<SessionEntity> set : session.values())
            for (SessionEntity sm : set) {
                //发送者所在客户端 不需要发送给自己
                if (sm.redisEntity.uuid.equals(msg.from) && sm.redisEntity.clientToken == msg.fromToken)
                    continue;
                sendMsgReal(msg, sm);
            }
    }

    private void sendMsgNormal(NimMsg msg, Set<String> toList) {
        Set<SessionEntity> smSet = new HashSet<>();
        for (String to : toList)
            smSet.addAll(session.get(to));
        if (CollectionUtils.isEmpty(smSet)) {
            L.e("sendMsgLocal 查找uuid错误");
        }

        for (SessionEntity sm : smSet) {
            //发送者所在客户端 不需要发送给自己
            if (sm.redisEntity.uuid.equals(msg.from) && sm.redisEntity.clientToken == msg.fromToken)
                continue;
            sendMsgReal(msg, sm);
        }
    }

    private void sendMsgReal(NimMsg msg, SessionEntity sm) {
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

    public interface GroupMember {
        /**
         * 获取群组的uuid 用于群成员的消息推送
         */
        Set<String> memberUuid(String groupId);
    }
}
