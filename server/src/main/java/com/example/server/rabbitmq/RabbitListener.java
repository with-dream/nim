package com.example.server.rabbitmq;

import com.example.server.netty.SessionHolder;
import com.example.server.netty.SessionModel;
import com.example.server.service.UserService;
import com.rabbitmq.client.Channel;
import netty.model.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import user.CacheModel;
import user.FriendModel;
import user.GroupMapModel;
import user.GroupModel;
import utils.L;

import javax.annotation.Resource;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Component(RabbitListener.LISTENER_TAG)
public class RabbitListener implements ChannelAwareMessageListener {
    public static final String LISTENER_TAG = "RabbitListener";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserService userService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        //TODO 具体操作
    }

    private void sendMsgPerson(BaseMsgModel baseMsgModel) {
        //对各个端推送消息
        List<SessionModel> sessionModel = SessionHolder.sessionMap.get(baseMsgModel.to);
        List<SessionModel> sessionModelSelf = SessionHolder.sessionMap.get(baseMsgModel.from);

        if (sessionModel != null) {
            for (SessionModel session : sessionModel)
                if (session != null && session.channel != null) {
                    BaseMsgModel tmpModel = baseMsgModel.clone();
                    tmpModel.receiptTag = session.deviceTag;
                    session.channel.writeAndFlush(tmpModel);

                    ReceiptModel recModel = new ReceiptModel();
                    recModel.channel = new WeakReference<>(session.channel);
                    recModel.msgModel = baseMsgModel;
                    //加入回执缓存
                    SessionHolder.receiptMsg.put(baseMsgModel.msgId + session.deviceTag, recModel);
                }
        }
        //对于自己的非当前客户端 也要推送消息
        if (sessionModelSelf.size() > 1)
            for (SessionModel session : sessionModelSelf)
                //TODO 不发给发送者
                if (session != null && session.channel != null && !session.deviceTag.equals(baseMsgModel.receiptTag))
                    session.channel.writeAndFlush(baseMsgModel);
    }

    //TODO 可以优化 如果mq相同 则将to做成数组
    private void sendMsgGroup(BaseMsgModel msgModel) {
        List<SessionModel> seses = SessionHolder.sessionMap.get(msgModel.to);
        for (SessionModel s : seses) {
            if (s.deviceTag.equals(msgModel.receiptTag)) {
                BaseMsgModel tmpModel = msgModel.clone();
                tmpModel.receiptTag = s.deviceTag;
                s.channel.writeAndFlush(tmpModel);

                ReceiptModel recModel = new ReceiptModel();
                recModel.channel = new WeakReference<>(s.channel);
                recModel.msgModel = msgModel;
                SessionHolder.receiptMsg.put(msgModel.msgId + s.deviceTag, recModel);
            }
        }
    }

    private void process(BaseMsgModel baseMsgModel) {
        switch (baseMsgModel.type) {
            case MsgType.REQ_CMD_MSG:
                RequestMsgModel reqMsg = (RequestMsgModel) baseMsgModel;
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
                    case RequestMsgModel.GROUP_ADD_AGREE:
                        addGroupMember(reqMsg);
                        break;
                    case RequestMsgModel.REQUEST_FRIEND_AGREE:
                        FriendModel friendModel = new FriendModel();
                        friendModel.userId = Math.min(reqMsg.from, reqMsg.to);
                        friendModel.friendId = Math.max(reqMsg.from, reqMsg.to);
                        friendModel.status = 1;
                        int res = userService.addFriend(friendModel);
                        if (res > 0) {
                            writeReqMsg(reqMsg);
                        }
                        break;
                    case RequestMsgModel.DEL_FRIEND:
                    case RequestMsgModel.DEL_FRIEND_EACH:
                    case RequestMsgModel.DEL_FRIEND_BLOCK:
                    case RequestMsgModel.DEL_FRIEND_UNBLOCK:
                        FriendModel delModel = new FriendModel();
                        delModel.userId = Math.min(reqMsg.from, reqMsg.to);
                        delModel.friendId = Math.max(reqMsg.from, reqMsg.to);
                        FriendModel resCheck = userService.checkFriend(delModel.userId, delModel.friendId);
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

                        int delRes = userService.delFriend(delModel);
                        if (delRes > 0) {
                            reqMsg.status = delModel.status;
                            writeReqMsg(reqMsg);
                        }
                        break;
                }
                break;

            case MsgType.RECEIPT_MSG:
                ReceiptMsgModel recModel = (ReceiptMsgModel) baseMsgModel;
                //将回执消息存储
//                String receiptLine = "msg_receipt:" + Math.min(baseMsgModel.from, baseMsgModel.to) + ":" + Math.max(baseMsgModel.from, baseMsgModel.to);
//                that.redisTemplate.opsForList().rightPush(receiptLine, gson.toJson(baseMsgModel));
                //分发回执消息
                //需要区分群消息 个人消息等
                List<SessionModel> sessionModelSelfReceipt = SessionHolder.sessionMap.get(baseMsgModel.to);
                for (SessionModel session : sessionModelSelfReceipt)
                    if (session != null && session.channel != null && !session.deviceTag.equals(baseMsgModel.receiptTag))
                        session.channel.writeAndFlush(baseMsgModel);

//                L.p("RECEIPT_MSG==>" + SessionHolder.receiptMsg.toString());
//                L.p("RECEIPT_MSG 111==>" + (recModel.receipt + baseMsgModel.receiptTag));
                SessionHolder.receiptMsg.remove(recModel.receipt + baseMsgModel.receiptTag);
                break;
        }
    }


    private void requestFriend(RequestMsgModel msgModel) {
        FriendModel friendModel = userService.checkFriend(msgModel.from, msgModel.to);
        //如果已经是好友 则直接返回好友信息
        if (friendModel != null) {
            msgModel.cmd = RequestMsgModel.REQUEST_FRIEND_FRIEND;
//            channel.writeAndFlush(msgModel); //TODO
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
//            writeReqMsg(msgModel, channel); //TODO
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
                session.channel.writeAndFlush(msgModel);
            }
        }
    }

    private boolean cacheModel(String timeLineID, CacheModel cacheModel) {
//        List<CacheModel> cacheList = cacheMsg.get(timeLineID);
//        if (cacheList == null) {
//            synchronized (timeLineID.intern()) {
//                if (cacheList == null) {
//                    cacheList = Collections.synchronizedList(new LinkedList<>());
//                    cacheMsg.put(timeLineID, cacheList);
//                }
//
//                return cacheList.add(cacheModel);
//            }
//        }

//        return cacheList.add(cacheModel);
        return true;
    }

    //删除缓存消息 可以删除超过7天的消息
    private boolean delCacheModel(String timeLineID) {
//        List<CacheModel> cacheList = cacheMsg.get(timeLineID);
//        if (cacheList == null) {
//            return true;
//        }


        return true;
    }

}
