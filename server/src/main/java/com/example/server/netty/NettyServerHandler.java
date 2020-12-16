package com.example.server.netty;

import com.example.server.ApplicationRunnerImpl;
import com.example.server.entity.MQMapModel;
import com.example.server.redis.RedissonUtil;
import com.example.server.service.UserService;
import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import netty.model.*;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import user.*;
import utils.L;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Gjing
 * <p>
 * netty服务端处理器
 **/
@Component
public class NettyServerHandler extends SimpleChannelInboundHandler<BaseMsgModel> {
    private static final int TRY_COUNT_MAX = 5;
    private Gson gson = new Gson();

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserService userService;

    @Resource
    RedissonUtil redissonUtil;

    private static NettyServerHandler that;
    private ConcurrentHashMap<String, List<CacheModel>> cacheMsg = new ConcurrentHashMap<>();

    @Autowired
    private AmqpTemplate rabbit;

    @PostConstruct
    public void init() {
        that = this;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        closeChannle(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        ctx.executor().scheduleAtFixedRate(() -> {
            if (!SessionHolder.receiptMsg.isEmpty()) {
                SessionHolder.receiptMsg.forEach((k, v) -> {
                    if (v.channel != null && v.channel.get() != null) {
                        v.channel.get().writeAndFlush(v);
                        v.msgModel.tryCount++;

                        if (v.msgModel.tryCount >= TRY_COUNT_MAX) {
                            L.e("重发失败==>" + v.toString());
                            SessionHolder.receiptMsg.remove(k);
                        }
                    } else {
                        L.e("重发失败 channel为空==>" + v.toString());
                        SessionHolder.receiptMsg.remove(k);
                    }

                });
            }
        }, 5, 8, TimeUnit.SECONDS);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseMsgModel baseMsgModel) throws Exception {
        if (!(baseMsgModel instanceof CmdMsgModel) || ((CmdMsgModel) baseMsgModel).cmd != CmdMsgModel.HEART)
            L.p("channelRead0==>" + baseMsgModel.toString());
        switch (baseMsgModel.type) {
            case MsgType.CMD_MSG:
                CmdMsgModel cmdMsg = (CmdMsgModel) baseMsgModel;
                switch (cmdMsg.cmd) {
                    case CmdMsgModel.LOGIN:
                        SessionHolder.login(ctx.channel(), baseMsgModel);
                        System.err.println("login==>" + baseMsgModel.toString());

                        sendRabbitLogin(cmdMsg);
                        break;
                    case CmdMsgModel.LOGOUT:
                        SessionHolder.unlogin(ctx.channel());
                        ctx.channel().close();

                        sendRabbitLogin(cmdMsg);
                        break;
                    case CmdMsgModel.HEART:
                        cmdMsg.to = cmdMsg.from;
                        cmdMsg.from = 0;
                        ctx.channel().writeAndFlush(cmdMsg);
                        break;
                }
                break;
            case MsgType.MSG_PERSON:
                MsgModel person = (MsgModel) baseMsgModel;
                Map<String, MQMapModel> mapPModel = (Map<String, MQMapModel>) redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, person.to);
                if (mapPModel == null) {
                    int check = that.userService.checkUser(person.to);
                    if (check == 0) {
                        //TODO 如果uuid不存在 则丢弃 否则缓存
                        System.err.println("不存在的uuid==>" + person.to);
                        break;
                    }
                }

                for (MQMapModel value : mapPModel.values()) {
                    that.rabbit.convertAndSend(value.queueName, gson.toJson(person));
                }

                //缓存消息
                String timeLineID = "msg_p:" + Math.min(person.from, person.to) + ":" + Math.max(person.from, person.to);
                that.redisTemplate.opsForList().rightPush(timeLineID, gson.toJson(person));
//                System.err.println("timeLineID==>" + that.redisTemplate.opsForList().leftPop(timeLineID));
                break;
            case MsgType.MSG_GROUP:
                MsgModel msgModel = (MsgModel) baseMsgModel;
                GroupModel groupModel = that.userService.getGroupInfo(msgModel.to);
                List<GroupMember> members = groupModel.getMembers(gson);
                for (GroupMember m : members) {

                    Map<String, MQMapModel> mapGModel = (Map<String, MQMapModel>) redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, m.userId);
                    if (mapGModel == null) {
                        int check = that.userService.checkGroup(m.userId);
                        if (check == 0) {
                            //TODO 如果groupId不存在 则丢弃 否则缓存
                            System.err.println("不存在的uuid==>" + m.userId);
                            break;
                        }
                    }

                    for (MQMapModel value : mapGModel.values()) {
                        that.rabbit.convertAndSend(value.queueName, gson.toJson(msgModel));
                    }
                }

                String groupLine = "msg_group:" + baseMsgModel.to;
                that.redisTemplate.opsForList().rightPush(groupLine, gson.toJson(baseMsgModel));
                break;
            //TODO 需要写入mq中
            case MsgType.RECEIPT_MSG:
                ReceiptMsgModel recModel = (ReceiptMsgModel) baseMsgModel;
                //将回执消息存储
//                String receiptLine = "msg_receipt:" + Math.min(baseMsgModel.from, baseMsgModel.to) + ":" + Math.max(baseMsgModel.from, baseMsgModel.to);
//                that.redisTemplate.opsForList().rightPush(receiptLine, gson.toJson(baseMsgModel));
                //分发回执消息
                //需要区分群消息 个人消息等
                List<SessionModel> sessionModelSelfReceipt = SessionHolder.sessionMap.get(baseMsgModel.to);
                for (SessionModel session : sessionModelSelfReceipt)
                    if (session != null && session.channel != null && session.channel != ctx.channel())
                        session.channel.writeAndFlush(baseMsgModel);

//                L.p("RECEIPT_MSG==>" + SessionHolder.receiptMsg.toString());
//                L.p("RECEIPT_MSG 111==>" + (recModel.receipt + baseMsgModel.receiptTag));
                SessionHolder.receiptMsg.remove(recModel.receipt + baseMsgModel.receiptTag);
                break;
        }

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;

            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                closeChannle(ctx);
            }
        }
    }

    /**
     * 发生异常触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //TODO 将崩溃放入日志
        cause.printStackTrace();
        closeChannle(ctx);
    }

    private void closeChannle(ChannelHandlerContext ctx) {
        SessionHolder.unlogin(ctx.channel());
        ctx.close();
    }

    //TODO 修改redis中的对象 不知道是否需要重新设置回去
    private void sendRabbitLogin(CmdMsgModel cmdMsg) {
        RLock lock = that.redissonUtil.getLock(cmdMsg.from + "");
        lock.lock();
        Map<String, MQMapModel> map = (Map) that.redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, cmdMsg.from);
        if (cmdMsg.cmd == CmdMsgModel.LOGIN) {
            if (map == null) {
                map = new HashMap<>();
                that.redisTemplate.opsForHash().put(ApplicationRunnerImpl.MQ_TAG, cmdMsg.from, map);
            }
            MQMapModel mapModel = new MQMapModel();
            mapModel.uuid = cmdMsg.from + "";
            mapModel.queueName = ApplicationRunnerImpl.MQ_NAME;
            mapModel.deviceToken = cmdMsg.loginTag;
            map.put(mapModel.deviceToken, mapModel);
        } else if (cmdMsg.cmd == CmdMsgModel.LOGOUT) {
            if (map != null) {
                map.remove(cmdMsg.loginTag);
            }
        }
        lock.unlock();
    }
}