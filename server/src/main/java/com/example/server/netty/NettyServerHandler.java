package com.example.server.netty;

import com.example.server.entity.GroupMsgModel;
import com.example.server.redis.TagList;
import com.example.server.service.RequestService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import netty.model.*;
import org.springframework.stereotype.Component;
import utils.Constant;
import utils.L;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author Gjing
 * <p>
 * netty服务端处理器
 **/
@Component
public class NettyServerHandler extends SimpleChannelInboundHandler<BaseMsgModel> {
    private static final int TRY_COUNT_MAX = 5;

    public static final int WEEK_SECOND = 7 * 24 * 60 * 60;
    public static final int MONTH_SECOND = 30 * 24 * 60 * 60;

    private static NettyServerHandler that;

    @PostConstruct
    public void init() {
        that = this;
    }

    @Resource
    public RequestService requestService;

    @Resource
    public SessionServerHolder holder;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        that.holder.logout(ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        ctx.executor().scheduleAtFixedRate(() -> {
            if (!SessionHolder.receiptMsg.isEmpty()) {
                SessionHolder.receiptMsg.forEach((k, v) -> {
                    if (System.currentTimeMillis() - v.msgModel.sendTime < (v.msgModel.tryCount + 1) * 500)
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
        if (!(baseMsgModel instanceof MsgModel) || ((MsgModel) baseMsgModel).cmd != MsgCmd.HEART) {
//            L.p("channelRead0==>" + baseMsgModel.toString());
            //除心跳包以外 每收到一个消息都要回复服务端已收到
            ReceiptMsgModel recvModel = ReceiptMsgModel.create(Constant.SERVER_UID, baseMsgModel.to, baseMsgModel.msgId, Constant.SERVER_TOKEN);
            recvModel.sendMsgType = baseMsgModel.type;
            recvModel.cmd = MsgCmd.SERVER_RECEIVED;
            ctx.channel().write(recvModel);
        }

        if (!(baseMsgModel instanceof MsgModel) || ((MsgModel) baseMsgModel).cmd != MsgCmd.HEART)
            L.p("server==>" + baseMsgModel);

        baseMsgModel.timestamp = System.currentTimeMillis();
        switch (baseMsgModel.type) {
            case MsgType.MSG_CMD:
                MsgModel cmdMsg = (MsgModel) baseMsgModel;
                switch (cmdMsg.cmd) {
                    case MsgCmd.LOGIN:
                        that.holder.login(ctx.channel(), cmdMsg);
                        that.holder.sendOfflineMsg(cmdMsg.from);
                        break;
                    case MsgCmd.LOGOUT:
                        that.holder.logout(ctx.channel());
                        break;
                    case MsgCmd.HEART:
                        cmdMsg.to = cmdMsg.from;
                        cmdMsg.from = Constant.SERVER_UID;
                        ctx.channel().writeAndFlush(cmdMsg);
                        break;
                    default:

                        break;
                }
                break;
            case MsgType.MSG_CMD_REQ:
                RequestMsgModel reqMsg = (RequestMsgModel) baseMsgModel;
//                String timeLineID = StrUtil.getTimeLine(reqMsg.from, reqMsg.to, "msg_req");
//                that.holder.sendMsq(reqMsg, ctx.channel(), TagList.TAG_REQ, true);
                that.requestService.requestMsg(reqMsg, ctx.channel());
//                //查找接受用户的uuid 获取信息
//                List<SessionRedisModel> reqSession = that.holder.getSessionRedis(Collections.singletonList(reqMsg.to));
//                //用户不在线 缓存消息
//                if (reqSession.isEmpty()) {
//                    int check = that.userService.checkUser(reqMsg.to);
//                    if (check == 0) {
//                        //TODO 如果uuid不存在 则丢弃 否则缓存
//                        System.err.println("不存在的uuid  MSG_CMD_REQ==>" + reqMsg.to);
//                        resCode = Errcode.NOBODY;
//                        break;
//                    }
//                    reqMsg.status = BaseMsgModel.OFFLINE;
//                    String timeLineID = StrUtil.getTimeLine(reqMsg.from, reqMsg.to, "msg_req");
//                    that.holder.saveOfflineMsgId(ctx.channel(), reqMsg, timeLineID);
//                    resCode = Errcode.OFFLINE;
//                    break;
//                }
//
//                SessionHolder.sendMsg(reqMsg, false);
//
//                //转发到其他服务器
//                Set<String> queueTmpReq = new HashSet<>();
//                //去除本服务器
//                queueTmpReq.add(ApplicationRunnerImpl.MQ_NAME);
//                for (SessionRedisModel session : reqSession) {
//                    //如果多个客户端在同一个服务器 只需要发送一份
//                    if (queueTmpReq.contains(session.queueName))
//                        continue;
//                    queueTmpReq.add(session.queueName);
//
//                    that.rabbit.convertAndSend(session.queueName, gson.toJson(new MQWrapper(MsgType.MSG_CMD_REQ, gson.toJson(reqMsg))));
//                }
                break;
            case MsgType.MSG_PERSON:
                MsgModel perMsg = (MsgModel) baseMsgModel;
                that.holder.sendMsq(perMsg, ctx.channel(), TagList.TAG_PERSON, true);
//                //查找接受用户的uuid 获取信息
//                List<SessionRedisModel> perSession = that.holder.getSessionRedis(Arrays.asList(perMsg.to, perMsg.from));
//
//                //用户不在线 缓存消息
//                boolean toEmpty = true;
//                for (SessionRedisModel srm : perSession)
//                    if (srm.uuid.equals(perMsg.to)) {
//                        toEmpty = false;
//                        break;
//                    }
//                if (toEmpty) {
//                    int check = that.userService.checkUser(perMsg.to);
//                    if (check == 0) {
//                        //TODO 如果uuid不存在 则丢弃 否则缓存
//                        System.err.println("不存在的uuid  MSG_CMD_REQ==>" + perMsg.to);
//                        resCode = Errcode.NOBODY;
//                        break;
//                    }
//                    perMsg.status = BaseMsgModel.OFFLINE;
//                String timeLineId = StrUtil.getTimeLine(perMsg.from, perMsg.to, "msg_per");
//                    that.holder.saveOfflineMsgId(ctx.channel(), perMsg, timeLineID);
//                    resCode = Errcode.OFFLINE;
//                    break;
//                }
//
//                SessionHolder.sendMsg(perMsg, true);
//
//                //转发到其他服务器
//                Set<String> queueTmpPer = new HashSet<>();
//                //去除本服务器
//                queueTmpPer.add(ApplicationRunnerImpl.MQ_NAME);
//                for (SessionRedisModel session : perSession) {
//                    //如果多个客户端在同一个服务器 只需要发送一份
//                    if (queueTmpPer.contains(session.queueName))
//                        continue;
//                    queueTmpPer.add(session.queueName);
//
//                    that.rabbit.convertAndSend(session.queueName, gson.toJson(new MQWrapper(perMsg.type, gson.toJson(perMsg))));
//                }
//                //缓存消息
//                that.holder.saveMsg(timeLineID, baseMsgModel);
                break;
            case MsgType.MSG_GROUP:
                GroupMsgModel msgModel = (GroupMsgModel) baseMsgModel;
                that.holder.sendGroupMsq(msgModel);
//                //获取群信息
//                GroupModel groupModel = that.userService.getGroupInfo(msgModel.groupId);
//                if (groupModel == null) {
//                    int check = that.userService.checkGroup(groupModel.userId);
//                    if (check == 0) {
//                        //TODO 如果groupId不存在 则丢弃 否则缓存
//                        System.err.println("不存在的uuid  MSG_GROUP==>" + groupModel.userId);
//                        break;
//                    }
//                }
//                //获取群成员
//                List<GroupMember> members = groupModel.getMembers(gson);
//                List<String> uuidList = new ArrayList<>();
//                for (GroupMember m : members)
//                    uuidList.add(m.userId);
//                //获取所有的在线成员 并将相同queueName的成员
//                List<SessionRedisModel> memSessionList = that.holder.getSessionRedis(uuidList);
//                if (!memSessionList.isEmpty()) {
//                    Map<String, GroupMsgModel> gMap = new HashMap<>();
//                    for (SessionRedisModel srm : memSessionList) {
//                        //先推送连接本服务器的客户端
//                        if (srm.queueName.equals(ApplicationRunnerImpl.MQ_NAME)
//                                && srm.clientToken != msgModel.fromToken) {
//                            GroupMsgModel groupMsg = GroupMsgModel.createG(msgModel.from, srm.uuid);
//                            SessionHolder.sendMsg(groupMsg, false);
//                        } else {
//                            //将相同服务器的所有目标uuid打包 统一发送
//                            GroupMsgModel gmm = gMap.get(srm.queueName);
//                            if (gmm == null) {
//                                gmm = new GroupMsgModel();
//                                gMap.put(srm.queueName, gmm);
//                            }
//                            gmm.toSet.add(srm.uuid);
//                        }
//                    }
//                    //发送到其他服务器
//                    for (Map.Entry<String, GroupMsgModel> entry : gMap.entrySet())
//                        that.rabbit.convertAndSend(entry.getKey(), gson.toJson(new MQWrapper(MsgType.MSG_CMD_REQ, gson.toJson(entry.getValue()))));
//                }
//
//                that.holder.saveMsg(groupLine, baseMsgModel);
                break;
            //回执消息
            case MsgType.MSG_RECEIPT:
                ReceiptMsgModel recModel = (ReceiptMsgModel) baseMsgModel;
                SessionHolder.receiptMsg.remove(recModel.token());

                String timeLineTag = TagList.TAG_REC_UNKNOW;
                switch (recModel.sendMsgType) {
                    case MsgType.MSG_GROUP:
                        //TODO 群消息太多 可以不用回执
                        timeLineTag = TagList.TAG_REC_GROUP;
                        break;
                    case MsgType.MSG_PERSON:
                        timeLineTag = TagList.TAG_REC_PERSON;
                        break;
                    case MsgType.MSG_PACK:
                        //TODO 删除离线id
                        timeLineTag = TagList.TAG_REC_PACK;
                        break;
                    case MsgType.MSG_CMD_REQ:
                        timeLineTag = TagList.TAG_REC_REQ;
                        break;
                }
                that.holder.sendMsq(recModel, ctx.channel(), timeLineTag, false);
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
                that.holder.logout(ctx.channel());
            }
        }
    }

    /**
     * 发生异常触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //TODO 将崩溃放入日志
        that.holder.logout(ctx.channel());
        cause.printStackTrace();
    }
}
