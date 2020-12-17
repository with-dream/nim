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
import netty.MQWrapper;
import netty.model.*;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import user.*;
import utils.L;
import utils.StrUtil;

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
                        cmdMsg.from = "";
                        ctx.channel().writeAndFlush(cmdMsg);
                        break;
                    default:
                        Map<String, MQMapModel> reqMap = (Map<String, MQMapModel>) redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, cmdMsg.to);
                        if (reqMap == null) {
                            int check = that.userService.checkUser(cmdMsg.to);
                            if (check == 0) {
                                //TODO 如果uuid不存在 则丢弃 否则缓存
                                System.err.println("不存在的uuid==>" + cmdMsg.to);
                                break;
                            }
                        }

                        sendMQ(reqMap, MsgType.CMD_MSG, cmdMsg);
                        break;
                }
                break;
            case MsgType.REQ_CMD_MSG:
                RequestMsgModel reqMsg = (RequestMsgModel) baseMsgModel;
                Map<String, MQMapModel> reqMap = (Map<String, MQMapModel>) redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, reqMsg.to);
                if (reqMap == null) {
                    int check = that.userService.checkUser(reqMsg.to);
                    if (check == 0) {
                        //TODO 如果uuid不存在 则丢弃 否则缓存
                        System.err.println("不存在的uuid==>" + reqMsg.to);
                        break;
                    }
                }

                sendMQ(reqMap, MsgType.REQ_CMD_MSG, reqMsg);
                break;
            case MsgType.MSG_PERSON:
                baseMsgModel.timestamp = System.currentTimeMillis();
                MsgModel person = (MsgModel) baseMsgModel;
                Map<String, MQMapModel> mapPModelTo = (Map<String, MQMapModel>) redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, person.to);
                Map<String, MQMapModel> mapPModelFrom = (Map<String, MQMapModel>) redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, person.from);
                if (mapPModelTo == null) {
                    int check = that.userService.checkUser(person.to);
                    if (check == 0) {
                        //TODO 如果uuid不存在 则丢弃 否则缓存
                        System.err.println("不存在的uuid==>" + person.to);
                        break;
                    }
                }

                sendMQ(mapPModelTo, MsgType.MSG_PERSON, person);

                Set<String> tmpPSet = new HashSet();
                for (MQMapModel value : mapPModelFrom.values()) {
                    if (tmpPSet.contains(value.queueName))
                        continue;
                    tmpPSet.add(value.queueName);

                    if (value.clientToken != baseMsgModel.fromToken)
                        that.rabbit.convertAndSend(value.queueName, gson.toJson(new MQWrapper(MsgType.MSG_PERSON, gson.toJson(person), MQWrapper.SELF)));
                }
                //缓存消息
                String timeLineID = StrUtil.getTimeLine(person.from, person.to, "msg_p");
                that.redisTemplate.opsForList().rightPush(timeLineID, gson.toJson(person));
//                System.err.println("timeLineID==>" + that.redisTemplate.opsForList().leftPop(timeLineID));
                break;
            case MsgType.MSG_GROUP:
                baseMsgModel.timestamp = System.currentTimeMillis();
                MsgModel msgModel = (MsgModel) baseMsgModel;
                GroupModel groupModel = that.userService.getGroupInfo(msgModel.groupId);
                List<GroupMember> members = groupModel.getMembers(gson);
                Set<String> tmpGSet = new HashSet();

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

                    boolean self = m.userId.equals(baseMsgModel.from);
                    for (MQMapModel value : mapGModel.values()) {
                        if (self && value.clientToken == baseMsgModel.fromToken)
                            continue;
                        if (tmpGSet.contains(value.queueName))
                            continue;
                        tmpGSet.add(value.queueName);

                        //指向目标uuid
                        msgModel.to = value.uuid;
                        that.rabbit.convertAndSend(value.queueName, gson.toJson(new MQWrapper(MsgType.MSG_GROUP, gson.toJson(msgModel), self ? 1 : 0)));
                    }

                    mapGModel.clear();
                }

                String groupLine = "msg_g:" + baseMsgModel.to;
                that.redisTemplate.opsForList().rightPush(groupLine, gson.toJson(baseMsgModel));
                break;
            case MsgType.RECEIPT_MSG:
                ReceiptMsgModel recModel = (ReceiptMsgModel) baseMsgModel;
                Map<String, MQMapModel> recMap = (Map<String, MQMapModel>) redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, recModel.to);
                if (recMap == null) {
                    int check = that.userService.checkUser(recModel.to);
                    if (check == 0) {
                        //TODO 如果uuid不存在 则丢弃 否则缓存
                        System.err.println("不存在的uuid==>" + recModel.to);
                        break;
                    }
                }

                sendMQ(recMap, MsgType.RECEIPT_MSG, recModel);
                break;
            default:
                L.e("未定义指令==>" + baseMsgModel.toString());
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
        closeChannle(ctx);
        cause.printStackTrace();
    }

    private void closeChannle(ChannelHandlerContext ctx) {
        SessionHolder.unlogin(ctx.channel());
        ctx.close();
    }

    private void sendMQ(Map<String, MQMapModel> mqMap, int type, BaseMsgModel msg) {
        Set<String> queueSet = new HashSet<>();
        for (MQMapModel value : mqMap.values()) {
            if (queueSet.contains(value.queueName))
                break;
            queueSet.add(value.queueName);

            that.rabbit.convertAndSend(value.queueName, gson.toJson(new MQWrapper(type, gson.toJson(msg))));
        }
    }

    //TODO 修改redis中的对象 不知道是否需要重新设置回去
    private void sendRabbitLogin(CmdMsgModel cmdMsg) {
        RLock lock = that.redissonUtil.getLock(cmdMsg.from);
        lock.lock();
        Map<Integer, MQMapModel> map = (Map) that.redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, cmdMsg.from);
        if (cmdMsg.cmd == CmdMsgModel.LOGIN) {
            if (map == null) {
                map = new HashMap<>();
                that.redisTemplate.opsForHash().put(ApplicationRunnerImpl.MQ_TAG, cmdMsg.from, map);
            }
            //TODO 客户端唯一登录 需要踢掉已登录用户
            MQMapModel mapModel = new MQMapModel();
            mapModel.uuid = cmdMsg.from;
            mapModel.queueName = ApplicationRunnerImpl.MQ_NAME;
            mapModel.clientToken = cmdMsg.fromToken;
            mapModel.deviceType = cmdMsg.deviceType;
            map.put(mapModel.clientToken, mapModel);
        } else if (cmdMsg.cmd == CmdMsgModel.LOGOUT) {
            if (map != null) {
                map.remove(cmdMsg.fromToken);
            }
        }
        lock.unlock();
    }
}