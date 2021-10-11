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
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import user.*;
import utils.Constant;
import utils.L;
import utils.StrUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Gjing
 * <p>
 * netty服务端处理器
 **/
@Component
public class NettyServerHandler extends SimpleChannelInboundHandler<BaseMsgModel> {
    private static final int TRY_COUNT_MAX = 5;
    public static final String MSGID_MAP = "msg_map:";
    public static final String MSGID_OFFLINE = "offline_msgid:";
    public static final int WEEK_SECOND = 7 * 24 * 60 * 60;
    public static final int MONTH_SECOND = 30 * 24 * 60 * 60;
    private Gson gson = new Gson();

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserService userService;

    private RedissonUtil redissonUtil = new RedissonUtil();

    private static NettyServerHandler that;

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
    protected void channelRead0(ChannelHandlerContext ctx, BaseMsgModel baseMsgModel) {
        if (!(baseMsgModel instanceof CmdMsgModel) || ((CmdMsgModel) baseMsgModel).cmd != CmdMsgModel.HEART) {
//            L.p("channelRead0==>" + baseMsgModel.toString());
            //除心跳包以外 都要回复一个收到消息
            ReceiptMsgModel recvModel = ReceiptMsgModel.create(Constant.SERVER_UID, baseMsgModel.to, baseMsgModel.msgId, Constant.SERVER_TOKEN);
            recvModel.sendMsgType = baseMsgModel.type;
            recvModel.cmd = CmdMsgModel.SEND_SUC;
            ctx.channel().write(recvModel);
        }

        switch (baseMsgModel.type) {
            case MsgType.MSG_CMD:
                CmdMsgModel cmdMsg = (CmdMsgModel) baseMsgModel;
                switch (cmdMsg.cmd) {
                    case CmdMsgModel.LOGIN:
                        SessionHolder.login(ctx.channel(), baseMsgModel);
                        System.err.println("channelRead0 login==>" + baseMsgModel.toString());

                        sendRabbitLogin(cmdMsg);

                        sendOfflineMsg(cmdMsg.from);
                        break;
                    case CmdMsgModel.LOGOUT:
                        SessionHolder.unlogin(ctx.channel());
                        sendRabbitLogin(cmdMsg);
                        ctx.channel().close();
                        break;
                    case CmdMsgModel.HEART:
                        cmdMsg.to = cmdMsg.from;
                        cmdMsg.from = "";
                        ctx.channel().writeAndFlush(cmdMsg);
                        break;
                    default:
                        Map<String, MQMapModel> reqMap = (Map<String, MQMapModel>) that.redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, cmdMsg.to);
                        if (reqMap == null) {
                            int check = that.userService.checkUser(cmdMsg.to);
                            if (check == 0) {
                                //TODO 如果uuid不存在 则丢弃 否则缓存
                                System.err.println("不存在的uuid cmd==>" + cmdMsg.to);
                                break;
                            }
                        }

                        sendMQ(reqMap, MsgType.MSG_CMD, cmdMsg);
                        break;
                }
                break;
            case MsgType.MSG_CMD_REQ:
                RequestMsgModel reqMsg = (RequestMsgModel) baseMsgModel;
                String timeLineIDReq = StrUtil.getTimeLine(reqMsg.from, reqMsg.to, "msg_r");

                Map<String, MQMapModel> reqMap = (Map<String, MQMapModel>) that.redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, reqMsg.to);
                if (reqMap == null || reqMap.isEmpty()) {
                    int check = that.userService.checkUser(reqMsg.to);
                    if (check == 0) {
                        //TODO 如果uuid不存在 则丢弃 否则缓存
                        System.err.println("不存在的uuid  MSG_CMD_REQ==>" + reqMsg.to);
                        break;
                    }
                    baseMsgModel.status = BaseMsgModel.OFFLINE;
                    saveOfflineMsgId(ctx.channel(), baseMsgModel, timeLineIDReq);
                }

                sendMQ(reqMap, MsgType.MSG_CMD_REQ, reqMsg);

                saveMsg(timeLineIDReq, baseMsgModel);
                break;
            case MsgType.MSG_PERSON:
                baseMsgModel.timestamp = System.currentTimeMillis();
                MsgModel person = (MsgModel) baseMsgModel;
                String timeLineID = StrUtil.getTimeLine(person.from, person.to, "msg_p");

                Map<String, MQMapModel> mapPModelTo = (Map) that.redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, person.to);
                Map<String, MQMapModel> mapPModelFrom = (Map) that.redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, person.from);
                if (mapPModelTo == null || mapPModelTo.isEmpty()) {
                    int check = that.userService.checkUser(person.to);
                    if (check == 0) {
                        //TODO 如果uuid不存在 则丢弃 否则缓存
                        System.err.println("不存在的uuid MSG_PERSON==>" + person.to);
                        break;
                    }

                    baseMsgModel.status = BaseMsgModel.OFFLINE;
                    saveOfflineMsgId(ctx.channel(), baseMsgModel, timeLineID);
                } else {
                    sendMQ(mapPModelTo, MsgType.MSG_PERSON, person);
                }

                sendMQ(mapPModelFrom, MsgType.MSG_PERSON, person, MQWrapper.SELF);

                //缓存消息
                saveMsg(timeLineID, baseMsgModel);
                break;
            case MsgType.MSG_GROUP:
                baseMsgModel.timestamp = System.currentTimeMillis();
                String groupLine = "msg_g:" + baseMsgModel.to;
                MsgModel msgModel = (MsgModel) baseMsgModel;
                GroupModel groupModel = that.userService.getGroupInfo(msgModel.groupId);
                List<GroupMember> members = groupModel.getMembers(gson);
                Set<String> tmpGSet = new HashSet();

                for (GroupMember m : members) {
                    Map<String, MQMapModel> mapGModel = (Map<String, MQMapModel>) that.redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, m.userId);
                    if (mapGModel == null) {
                        int check = that.userService.checkGroup(m.userId);
                        if (check == 0) {
                            //TODO 如果groupId不存在 则丢弃 否则缓存
                            System.err.println("不存在的uuid  MSG_GROUP==>" + m.userId);
                            break;
                        }

                        baseMsgModel.status = BaseMsgModel.OFFLINE;
                        saveOfflineMsgId(ctx.channel(), baseMsgModel, groupLine);
                    } else {
                        mapGModel.clear();

                        boolean self = m.userId.equals(baseMsgModel.from);
                        for (MQMapModel value : mapGModel.values()) {
                            if (self && value.clientToken == baseMsgModel.fromToken)
                                continue;
                            if (tmpGSet.contains(value.queueName))
                                continue;
                            tmpGSet.add(value.queueName);

                            msgModel.to = value.uuid;
                            L.p("handler sendMQ MSG_GROUP mq:" + value.queueName + "  " + msgModel.toString());
                            that.rabbit.convertAndSend(value.queueName, gson.toJson(new MQWrapper(MsgType.MSG_GROUP, gson.toJson(msgModel), self ? 1 : 0)));
                        }
                    }
                }

                saveMsg(groupLine, baseMsgModel);
                break;
            case MsgType.MSG_RECEIPT:
                ReceiptMsgModel recModel = (ReceiptMsgModel) baseMsgModel;
                Map<String, MQMapModel> recMap = (Map<String, MQMapModel>) that.redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, recModel.to);
                if (recMap == null) {
                    int check = that.userService.checkUser(recModel.to);
                    if (check == 0) {
                        //TODO 如果uuid不存在 则丢弃 否则缓存
                        System.err.println("不存在的uuid  MSG_RECEIPT==>" + recModel.to);
                        break;
                    }

                    baseMsgModel.status = BaseMsgModel.OFFLINE;
                }

                switch (recModel.sendMsgType) {
                    case MsgType.MSG_PERSON:
                        String timeLineP = StrUtil.getTimeLine(recModel.from, recModel.to, "msg_p");
                        saveMsg(timeLineP, recModel);

                        saveOfflineMsgId(ctx.channel(), recModel, timeLineP);
                        break;
                    case MsgType.MSG_GROUP:
                        String timeLineG = StrUtil.getTimeLine(recModel.from, recModel.to, "msg_g");
                        saveMsg(timeLineG, recModel);

                        saveOfflineMsgId(ctx.channel(), recModel, timeLineG);
                        break;
                    case MsgType.MSG_PACK:
                        //TODO 删除离线id
                        break;
                    case MsgType.MSG_CMD_REQ:
                        String timeLineR = StrUtil.getTimeLine(recModel.from, recModel.to, "msg_r");
                        saveMsg(timeLineR, recModel);

                        saveOfflineMsgId(ctx.channel(), recModel, timeLineR);
                        break;
                }

                sendMQ(recMap, MsgType.MSG_RECEIPT, recModel);
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
        String uuid = SessionHolder.sessionChannelMap.remove(ctx.channel());
        if (uuid == null)
            return;
        Vector<SessionModel> session = SessionHolder.sessionMap.get(uuid);
        session.removeIf(model -> {
            if (model.channel == ctx.channel()) {

                logout(model.clientToken, uuid);

                return true;
            }
            return false;
        });

        ctx.close();
    }

    private void sendMQ(Map<String, MQMapModel> mqMap, int type, BaseMsgModel msg) {
        this.sendMQ(mqMap, type, msg, 0);
    }

    private void sendMQ(Map<String, MQMapModel> mqMap, int type, BaseMsgModel msg, int self) {
        Set<String> queueSet = new HashSet<>();
        L.p("==>sendMQ");
        for (MQMapModel value : mqMap.values()) {
//            L.p("handler sendMQ mq:" + value.queueName + "  " + msg.toString());
            if (queueSet.contains(value.queueName))
                continue;
            if (value.clientToken == msg.fromToken && msg.type != MsgType.MSG_PACK)
                continue;
            queueSet.add(value.queueName);

            L.p("handler sendMQ mq  111:" + value.queueName + "  " + msg.toString());

            that.rabbit.convertAndSend(value.queueName, gson.toJson(new MQWrapper(type, gson.toJson(msg), self)));
        }
    }

    private void sendRabbitLogin(CmdMsgModel cmdMsg) {
        if (cmdMsg.cmd == CmdMsgModel.LOGIN) {
            login(cmdMsg);
        } else if (cmdMsg.cmd == CmdMsgModel.LOGOUT) {
            logout(cmdMsg.fromToken, cmdMsg.from);
        }
    }

    private void login(CmdMsgModel cmdMsg) {
//        RLock lock = redissonUtil.getLock(cmdMsg.from);
//        lock.lock();
        Map<Integer, MQMapModel> map = (Map) that.redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, cmdMsg.from);
        if (cmdMsg.cmd == CmdMsgModel.LOGIN) {
            if (map == null || (map.isEmpty() && !(map instanceof HashMap)))
                map = new HashMap<>();
            //TODO 客户端唯一登录 需要踢掉已登录用户
            MQMapModel mapModel = new MQMapModel();
            mapModel.uuid = cmdMsg.from;
            mapModel.queueName = ApplicationRunnerImpl.MQ_NAME;
            mapModel.clientToken = cmdMsg.fromToken;
            mapModel.deviceType = cmdMsg.deviceType;
            map.put(mapModel.clientToken, mapModel);
            L.e("login==>" + cmdMsg.toString());
            that.redisTemplate.opsForHash().put(ApplicationRunnerImpl.MQ_TAG, cmdMsg.from, map);
            that.redisTemplate.opsForSet().add(ApplicationRunnerImpl.HOST_NAME, cmdMsg.from + ":" + cmdMsg.fromToken);
        }
//        lock.unlock();
    }

    public void logout(long token, String uuid) {
//        RLock lock = redissonUtil.getLock(cmdMsg.from);
//        lock.lock();
        Map<Integer, MQMapModel> map = (Map) that.redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, uuid);
        if (map != null) {
            map.remove(token);
        }

        if (map == null || map.isEmpty())
            that.redisTemplate.opsForHash().delete(ApplicationRunnerImpl.MQ_TAG, uuid);
        else
            that.redisTemplate.opsForHash().put(ApplicationRunnerImpl.MQ_TAG, uuid, map);

        that.redisTemplate.opsForList().remove(ApplicationRunnerImpl.HOST_NAME, 1, uuid + ":" + token);

//        lock.unlock();
    }

    private boolean saveMsg(String timeLine, BaseMsgModel msgModel) {
        Long index = that.redisTemplate.opsForList().rightPush(timeLine, msgModel);
        if (index == null) {
            L.e("saveMsg==>存储消息失败");
            return false;
        }
        that.redisTemplate.opsForList().rightPush(MSGID_MAP + timeLine, msgModel.msgId);

        return true;
    }

    private boolean saveOfflineMsgId(Channel channel, BaseMsgModel msgModel, String timeLine) {
        if (msgModel.status == BaseMsgModel.OFFLINE) {
            that.redisTemplate.opsForHash().put(MSGID_OFFLINE + msgModel.to, msgModel.msgId, timeLine);

            //TODO 离线回复已发送 可以和SEND_SUC一起回复
            ReceiptMsgModel receiptModel = ReceiptMsgModel.create(msgModel.to, msgModel.from, msgModel.msgId, Constant.SERVER_TOKEN);
            receiptModel.cmd = CmdMsgModel.RECEIVED;
            receiptModel.sendMsgType = msgModel.type;
            receiptModel.toToken = msgModel.fromToken;
            channel.writeAndFlush(receiptModel);
        }
        return true;
    }

    private void sendOfflineMsg(String uuid) {
        Map<Object, Object> offMsg = that.redisTemplate.opsForHash().entries(MSGID_OFFLINE + uuid);
        if (offMsg.isEmpty())
            return;
        PackMsgModel msgModel = PackMsgModel.create(Constant.SERVER_UID, uuid, Constant.SERVER_TOKEN);

        Map<String, List<Long>> tmpMap = new HashMap<>();
        for (Map.Entry<Object, Object> entry : offMsg.entrySet()) {
            String timeline = (String) entry.getValue();
            List<Long> list = tmpMap.computeIfAbsent(timeline, k -> new ArrayList<>());
            list.add((Long) entry.getKey());
        }

        for (Map.Entry<String, List<Long>> entry : tmpMap.entrySet()) {
            String timeLineMap = MSGID_MAP + entry.getKey();
            String timeLineMsg = entry.getKey();
            List<Long> list = tmpMap.get(entry.getKey());
            for (long msgId : list) {
                Long index = that.redisTemplate.opsForList().indexOf(timeLineMap, msgId);
                if (index == null) continue;

                BaseMsgModel msg = (BaseMsgModel) that.redisTemplate.opsForList().index(timeLineMsg, index);
                if (msg != null) {
//                    msgModel.addMsg(gson.toJson(msg));
                } else {
                    L.e("sendOfflineMsg获取msg为null==>" + index);
                }

            }
        }

        L.e("sendOfflineMsg==>" + msgModel.toString());

        Map<String, MQMapModel> mapModel = (Map) that.redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, uuid);
        if (mapModel == null) {
            int check = that.userService.checkUser(uuid);
            if (check == 0) {
                //TODO 如果uuid不存在 则丢弃 否则缓存
                System.err.println("不存在的uuid sendOfflineMsg==>" + uuid);
            }
        }

        L.p("MSG_PACK==>" + msgModel.toString());
        sendMQ(mapModel, MsgType.MSG_PACK, msgModel);
    }
}
