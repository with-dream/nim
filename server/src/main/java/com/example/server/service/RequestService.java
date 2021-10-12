package com.example.server.service;

import com.example.server.ApplicationRunnerImpl;
import com.example.server.netty.SessionHolder;
import com.example.server.netty.SessionModel;
import com.example.server.netty.SessionServerHolder;
import netty.MQWrapper;
import netty.model.BaseMsgModel;
import netty.model.MsgType;
import netty.model.ReceiptModel;
import netty.model.RequestMsgModel;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import user.FriendModel;
import user.GroupMapModel;
import user.GroupModel;
import utils.Constant;
import utils.L;
import utils.StrUtil;

import javax.annotation.Resource;
import java.lang.ref.WeakReference;
import java.nio.channels.Channel;
import java.util.*;

@Service
public class RequestService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private AmqpTemplate rabbit;

    @Resource
    private SessionServerHolder holder;

    public boolean requestMsg(RequestMsgModel reqMsg, Channel channel) {
        switch (reqMsg.cmd) {
            case RequestMsgModel.REQUEST_FRIEND:
                requestFriend(reqMsg);
                break;
            case RequestMsgModel.GROUP_ADD:
                GroupModel group = userService.getGroupInfo(reqMsg.groupId);
                //申请群 将目标群指向为群拥有者
                reqMsg.to = group.userId;
                writeReqMsg(reqMsg);
                break;
            case RequestMsgModel.GROUP_EXIT:
                delGroupMember(reqMsg);
                break;
            case RequestMsgModel.GROUP_DEL:
                Vector<SessionModel> users = SessionHolder.sessionMap.get(reqMsg.to);
                if (users == null || users.isEmpty()) {
                    //TODO 加入缓存
                    break;
                }

                for (SessionModel ses : users) {
                    if (checkSelf(ses.clientToken, reqMsg))
                        continue;
                    RequestMsgModel msgModel = RequestMsgModel.create(Constant.SERVER_UID, reqMsg.to, ses.clientToken);
                    msgModel.groupId = reqMsg.groupId;
                    msgModel.cmd = RequestMsgModel.GROUP_DEL;
                    ses.channel.writeAndFlush(msgModel);

                    ReceiptModel receiptMsgModel = new ReceiptModel();
                    receiptMsgModel.channel = new WeakReference<>(ses.channel);
                    receiptMsgModel.msgModel = msgModel;
                    SessionHolder.receiptMsg.put(msgModel.msgId + "" + ses.clientToken, receiptMsgModel);
                }
                break;
            case RequestMsgModel.GROUP_ADD_AGREE:
                addGroupMember(reqMsg);
                break;
            case RequestMsgModel.REQUEST_FRIEND_AGREE:
                FriendModel friendModel = new FriendModel();
                String[] user = StrUtil.getStr(reqMsg.from, reqMsg.to);
                friendModel.userId = user[0];
                friendModel.friendId = user[1];
                friendModel.status = 1;
                int res = userService.addFriend(friendModel);
                if (res > 0) {
                    writeReqMsg(reqMsg);
                }
                break;
            case RequestMsgModel.FRIEND_DEL:
            case RequestMsgModel.FRIEND_DEL_EACH:
            case RequestMsgModel.FRIEND_DEL_BLOCK:
            case RequestMsgModel.FRIEND_DEL_UNBLOCK:
                FriendModel delModel = new FriendModel();
                FriendModel resCheck = userService.checkFriend(delModel.userId, delModel.friendId, true);
                //如果是双向好友
                if (resCheck.status == FriendModel.FRIEND_NORMAL) {
                    if (RequestMsgModel.FRIEND_DEL == reqMsg.cmd)
                        delModel.status = FriendModel.FRIEND_OTHER;
                    else if (RequestMsgModel.FRIEND_DEL_EACH == reqMsg.cmd)
                        delModel.status = FriendModel.FRIEND_DEL_EACH;
                    //如果是单向好友
                } else if (resCheck.status == FriendModel.FRIEND_SELF)
                    delModel.status = FriendModel.FRIEND_DEL_EACH;
                    //拉黑操作
                else if (RequestMsgModel.FRIEND_DEL_BLOCK == reqMsg.cmd) {
                    if (resCheck.status == FriendModel.FRIEND_BLOCK_OTHER)
                        delModel.status = FriendModel.FRIEND_BLOCK_EACH;
                    else if (resCheck.status != FriendModel.FRIEND_BLOCK_EACH)
                        delModel.status = FriendModel.FRIEND_SELF;
                    //解除拉黑 解除拉黑后 为删除好友的状态
                } else if (RequestMsgModel.FRIEND_DEL_UNBLOCK == reqMsg.cmd) {
                    if (resCheck.status == FriendModel.FRIEND_BLOCK_EACH)
                        delModel.status = FriendModel.FRIEND_OTHER;
                    else if (resCheck.status == FriendModel.FRIEND_BLOCK_SELF)
                        delModel.status = FriendModel.FRIEND_DEL_EACH;
                }

                int delRes = userService.delFriend(delModel);
                if (delRes > 0) {
                    reqMsg.status = delModel.status;
                    writeReqMsg(reqMsg);
                }
                break;
        }

        return true;
    }

    private void requestFriend(RequestMsgModel msgModel) {
        FriendModel friendModel = userService.checkFriend(msgModel.from, msgModel.to, true);
        if (friendModel != null && friendModel.status == ) {
            msgModel.cmd = RequestMsgModel.REQUEST_FRIEND_FRIEND;
//            channel.writeAndFlush(msgModel);
            return;
        }

        writeReqMsg(msgModel);
    }

    //加入群
    private void addGroupMember(RequestMsgModel msgModel) {
        int res = userService.addGroupMember(msgModel);
        if (res == 0) {
            GroupMapModel groupModel = new GroupMapModel();
            groupModel.userId = msgModel.to;
            groupModel.groupId = msgModel.groupId;
            L.p("member==>" + groupModel.toString());
            res = userService.addMapGroup(groupModel);
            if (res != 1)
                L.e("addGroupMember==>失败");
            writeReqMsg(msgModel);
        } else
            L.e("加入群错误");
    }

    //退群
    private void delGroupMember(RequestMsgModel msgModel) {
        GroupModel res = userService.delGroupMember(msgModel);
        if (res != null) {
            int resDel = userService.delMapGroup(msgModel.from);
            if (resDel != 1)
                L.e("delGroupMember==>失败");

            msgModel.to = res.userId;
            Map<String, MQMapModel> mapGModel = (Map<String, MQMapModel>) redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, msgModel.to);
            Set<String> tmpSet = new HashSet<>();
            for (MQMapModel value : mapGModel.values()) {
                if (tmpSet.contains(value.queueName))
                    continue;
                tmpSet.add(value.queueName);

                rabbit.convertAndSend(value.queueName, gson.toJson(new MQWrapper(MsgType.MSG_CMD_REQ, gson.toJson(msgModel))));
            }
        } else
            L.e("退群错误");
    }

    private void writeReqMsg(RequestMsgModel msgModel) {
        List<SessionModel> sessionModel = SessionHolder.sessionMap.get(msgModel.to);
        if (sessionModel == null) {
            int res = userService.checkUser(msgModel.to);
            if (res == 0) {
                System.err.println("==>requestFriend res 好友的uid为空");
                msgModel.cmd = RequestMsgModel.REQUEST_FRIEND_NOBODY;
//                channel.writeAndFlush(msgModel);//TODO
            }

            //加入缓存 用户登录时 直接拉取数据
            redisTemplate.opsForList().rightPush(String.valueOf(msgModel.to), msgModel);
        } else {
            for (SessionModel session : sessionModel) {
                if (checkSelf(session.clientToken, msgModel))
                    continue;
                session.channel.writeAndFlush(msgModel);
            }
        }
    }

    private boolean checkSelf(int token, BaseMsgModel msg) {
        return token == msg.fromToken;
    }
}
