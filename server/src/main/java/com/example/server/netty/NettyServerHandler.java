package com.example.server.netty;

import com.example.server.service.UserService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import netty.model.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import user.FriendModel;
import user.GroupMapModel;
import user.GroupModel;
import utils.L;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * @author Gjing
 * <p>
 * netty服务端处理器
 **/
@Component
public class NettyServerHandler extends SimpleChannelInboundHandler<BaseMsgModel> {
    private Gson gson = new Gson();

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserService userService;

    private static NettyServerHandler that;

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
    protected void channelRead0(ChannelHandlerContext ctx, BaseMsgModel baseMsgModel) throws Exception {
        if (!(baseMsgModel instanceof CmdMsgModel) || ((CmdMsgModel) baseMsgModel).cmd != CmdMsgModel.HEART)
            System.err.println("channelRead0==>" + baseMsgModel.toString());
        switch (baseMsgModel.type) {
            case MsgType.CMD_MSG:
                CmdMsgModel cmdMsg = (CmdMsgModel) baseMsgModel;
                switch (cmdMsg.cmd) {
                    case CmdMsgModel.LOGIN:
                        SessionHolder.login(ctx.channel(), baseMsgModel);
                        System.err.println("login==>" + baseMsgModel.toString());
                        break;
                    case CmdMsgModel.LOGOUT:
                        SessionHolder.unlogin(ctx.channel());
                        ctx.channel().close();
                        break;
                    case CmdMsgModel.HEART:
                        cmdMsg.to = cmdMsg.from;
                        cmdMsg.from = 0;
                        ctx.channel().writeAndFlush(cmdMsg);
                        break;
                }
                break;
            case MsgType.REQ_CMD_MSG:
                RequestMsgModel reqMsg = (RequestMsgModel) baseMsgModel;
                switch (reqMsg.cmd) {
                    case RequestMsgModel.REQUEST_FRIEND:
                        requestFriend(reqMsg, ctx.channel());
                        break;
                    case RequestMsgModel.GROUP_ADD:
                        GroupModel group = that.userService.getGroupInfo(reqMsg.groupId);
                        //申请群 将目标群指向为群拥有者
                        reqMsg.to = group.userId;
                        writeReqMsg(reqMsg, ctx.channel());
                        break;
                    case RequestMsgModel.GROUP_EXIT:
                        delGroupMember(reqMsg, ctx.channel());
                        break;
                    case RequestMsgModel.GROUP_ADD_AGREE:
                        addGroupMember(reqMsg, ctx.channel());
                        break;
                    case RequestMsgModel.REQUEST_FRIEND_AGREE:
                        FriendModel friendModel = new FriendModel();
                        friendModel.userId = Math.min(reqMsg.from, reqMsg.to);
                        friendModel.friendId = Math.max(reqMsg.from, reqMsg.to);
                        friendModel.status = 1;
                        int res = that.userService.addFriend(friendModel);
                        if (res > 0) {
                            writeReqMsg(reqMsg, ctx.channel());
                        }
                        break;
                    case RequestMsgModel.DEL_FRIEND:
                    case RequestMsgModel.DEL_FRIEND_EACH:
                    case RequestMsgModel.DEL_FRIEND_BLOCK:
                    case RequestMsgModel.DEL_FRIEND_UNBLOCK:
                        FriendModel delModel = new FriendModel();
                        delModel.userId = Math.min(reqMsg.from, reqMsg.to);
                        delModel.friendId = Math.max(reqMsg.from, reqMsg.to);
                        FriendModel resCheck = that.userService.checkFriend(delModel.userId, delModel.friendId);
                        //如果是双向好友
                        if (resCheck.status == FriendModel.FRIEND_NORMAL) {
                            if (RequestMsgModel.DEL_FRIEND == reqMsg.cmd)
                                delModel.status = FriendModel.FRIEND_OTHER;
                            else if (RequestMsgModel.DEL_FRIEND_EACH == reqMsg.cmd)
                                delModel.status = FriendModel.FRIEND_DEL;
                            //如果是单向好友
                        } else if (resCheck.status == FriendModel.FRIEND_SELF)
                            delModel.status = FriendModel.FRIEND_DEL;
                            //拉黑操作
                        else if (RequestMsgModel.DEL_FRIEND_BLOCK == reqMsg.cmd) {
                            if (resCheck.status == FriendModel.FRIEND_BLOCK_OTHER)
                                delModel.status = FriendModel.FRIEND_BLOCK;
                            else if (resCheck.status != FriendModel.FRIEND_BLOCK)
                                delModel.status = FriendModel.FRIEND_SELF;
                            //解除拉黑 解除拉黑后 为删除好友的状态
                        } else if (RequestMsgModel.DEL_FRIEND_UNBLOCK == reqMsg.cmd) {
                            if (resCheck.status == FriendModel.FRIEND_BLOCK)
                                delModel.status = FriendModel.FRIEND_OTHER;
                            else if (resCheck.status == FriendModel.FRIEND_BLOCK_SELF)
                                delModel.status = FriendModel.FRIEND_DEL;
                        }

                        int delRes = that.userService.delFriend(delModel);
                        if (delRes > 0) {
                            reqMsg.status = delModel.status;
                            writeReqMsg(reqMsg, ctx.channel());
                        }
                        break;
                }
                break;
            case MsgType.MSG_PERSON:
                MsgModel person = (MsgModel) baseMsgModel;
                List<SessionModel> sessionModel = SessionHolder.sessionMap.get(person.to);
                //为null表示未登录
                if (sessionModel == null) {
                    int check = that.userService.checkUser(person.to);
                    if (check == 0) {
                        //TODO 如果uuid不存在 则丢弃
                        System.err.println("不存在的uuid==>" + person.to);
                        break;
                    }
                }

                List<SessionModel> sessionModelSelf = SessionHolder.sessionMap.get(person.from);

                //缓存消息
                String timeLineID = "msg_person:" + Math.min(person.from, person.to) + ":" + Math.max(person.from, person.to);
                that.redisTemplate.opsForList().rightPush(timeLineID, gson.toJson(person));
                System.err.println("timeLineID==>" + that.redisTemplate.opsForList().leftPop(timeLineID));
                //对各个端推送消息
                if (sessionModel != null)
                    for (SessionModel session : sessionModel)
                        if (session != null && session.channel != null)
                            session.channel.writeAndFlush(person);
                //对于自己的非当前客户端 也要推送消息
                if (sessionModelSelf.size() > 1)
                    for (SessionModel session : sessionModelSelf)
                        if (session != null && session.channel != null && session.channel != ctx.channel())
                            session.channel.writeAndFlush(person);
                break;
            case MsgType.MSG_GROUP:
                MsgModel msgModel = (MsgModel) baseMsgModel;
                GroupModel groupModel = that.userService.getGroupInfo(msgModel.to);
                List<GroupMember> members = groupModel.getMembers(gson);
                for (GroupMember m : members) {
                    List<SessionModel> seses = SessionHolder.sessionMap.get(m.userId);
                    for (SessionModel s : seses) {
                        msgModel.to = m.userId;
                        if (s.channel != ctx.channel())
                            s.channel.writeAndFlush(msgModel);
                    }
                }

                String groupLine = "msg_group:" + baseMsgModel.to;
                that.redisTemplate.opsForList().rightPush(groupLine, gson.toJson(baseMsgModel));
                break;
            case MsgType.RECEIPT_MSG:
                //将回执消息存储
                String receiptLine = "msg_receipt:" + Math.min(baseMsgModel.from, baseMsgModel.to) + ":" + Math.max(baseMsgModel.from, baseMsgModel.to);
                that.redisTemplate.opsForList().rightPush(receiptLine, gson.toJson(baseMsgModel));
                //分发回执消息
                //需要区分群消息 个人消息等
                List<SessionModel> sessionModelSelfReceipt = SessionHolder.sessionMap.get(baseMsgModel.to);
                for (SessionModel session : sessionModelSelfReceipt)
                    if (session != null && session.channel != null && session.channel != ctx.channel())
                        session.channel.writeAndFlush(baseMsgModel);
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

    private void requestFriend(RequestMsgModel msgModel, Channel channel) {
        FriendModel friendModel = that.userService.checkFriend(msgModel.from, msgModel.to);
        //如果已经是好友 则直接返回好友信息
        if (friendModel != null) {
            msgModel.cmd = RequestMsgModel.REQUEST_FRIEND_FRIEND;
            channel.writeAndFlush(msgModel);
            return;
        }

        writeReqMsg(msgModel, channel);
    }

    //加入群
    private void addGroupMember(RequestMsgModel msgModel, Channel channel) {
        int res = that.userService.addGroupMember(msgModel);
        if (res == 0) {
            GroupMapModel groupModel = new GroupMapModel();
            groupModel.userId = msgModel.to;
            groupModel.groupId = msgModel.groupId;
            L.p("member==>" + groupModel.toString());
            res = that.userService.addMapGroup(groupModel);
            if (res != 1)
                L.e("addGroupMember==>失败");
            writeReqMsg(msgModel, channel);
        } else
            L.e("加入群错误");
    }

    //退群
    private void delGroupMember(RequestMsgModel msgModel, Channel channel) {
        GroupModel res = that.userService.delGroupMember(msgModel);
        if (res != null) {

            int resDel = that.userService.delMapGroup(msgModel.from);
            if (resDel != 1)
                L.e("delGroupMember==>失败");

            msgModel.to = res.userId;
            writeReqMsg(msgModel, channel);
        } else
            L.e("退群错误");
    }

    private void writeReqMsg(RequestMsgModel msgModel, Channel channel) {
        List<SessionModel> sessionModel = SessionHolder.sessionMap.get(msgModel.to);
        if (sessionModel == null) {
            int res = that.userService.checkUser(msgModel.to);
            if (res == 0) {
                System.err.println("==>requestFriend res 好友的uid为空");
                msgModel.cmd = RequestMsgModel.REQUEST_FRIEND_NOBODY;
                channel.writeAndFlush(msgModel);
            }

            //加入缓存 用户登录时 直接拉取数据
            redisTemplate.opsForList().rightPush(String.valueOf(msgModel.to), msgModel);
        } else {
            for (SessionModel session : sessionModel) {
                session.channel.writeAndFlush(msgModel);
            }
        }
    }
}