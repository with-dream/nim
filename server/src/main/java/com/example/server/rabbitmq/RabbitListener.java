package com.example.server.rabbitmq;

import com.example.server.ApplicationRunnerImpl;
import com.example.server.entity.MQMapModel;
import com.example.server.netty.SessionHolder;
import com.example.server.netty.SessionModel;
import com.example.server.service.UserService;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import netty.MQWrapper;
import netty.MessageDecode;
import netty.model.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import user.CacheModel;
import user.FriendModel;
import user.GroupMapModel;
import user.GroupModel;
import utils.Constant;
import utils.L;
import utils.StrUtil;

import javax.annotation.Resource;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

@Component(RabbitListener.LISTENER_TAG)
public class RabbitListener implements ChannelAwareMessageListener {
    public static final String LISTENER_TAG = "RabbitListener";

    private Gson gson = new Gson();

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserService userService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        byte[] bytes = message.getBody();
        MQWrapper mqWrapper = gson.fromJson(new String(bytes), MQWrapper.class);
        BaseMsgModel baseMsgModel = MessageDecode.getModel(gson, mqWrapper.type, mqWrapper.json);

        L.p("onMessage==>" + baseMsgModel.toString());

        process(baseMsgModel, mqWrapper.self);
    }

    private void process(BaseMsgModel baseMsgModel, int self) {
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
                    case RequestMsgModel.GROUP_DEL:
                        Vector<SessionModel> users = SessionHolder.sessionMap.get(reqMsg.to);
                        if (users == null || users.isEmpty()) {
                            //TODO 加入缓存
                            break;
                        }

                        for (SessionModel ses : users) {
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
                    case RequestMsgModel.DEL_FRIEND:
                    case RequestMsgModel.DEL_FRIEND_EACH:
                    case RequestMsgModel.DEL_FRIEND_BLOCK:
                    case RequestMsgModel.DEL_FRIEND_UNBLOCK:
                        FriendModel delModel = new FriendModel();
                        FriendModel resCheck = userService.checkFriend(delModel.userId, delModel.friendId, true);
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
            case MsgType.CMD_MSG:

                break;
            case MsgType.MSG_PERSON:
                sendMsgPerson(baseMsgModel, self);
                break;
            case MsgType.MSG_GROUP:
                sendMsgGroup(baseMsgModel);
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
                    if (session != null && session.channel != null)
                        session.channel.writeAndFlush(baseMsgModel);
                    else {
                        L.e("SessionModel==>为null");
                    }

//                L.p("RECEIPT_MSG==>" + SessionHolder.receiptMsg.toString());
//                L.p("RECEIPT_MSG 111==>" + (recModel.receipt + baseMsgModel.receiptTag));
                SessionHolder.receiptMsg.remove(recModel.sendMsgId + "" + recModel.fromToken);
                break;
        }
    }

    private void sendMsgPerson(BaseMsgModel baseMsgModel, int self) {
        boolean isSelf = self == MQWrapper.SELF;
        //对各个端推送消息
        List<SessionModel> sessionModel = SessionHolder.sessionMap.get(isSelf ? baseMsgModel.from : baseMsgModel.to);

        if (sessionModel != null) {
            for (SessionModel session : sessionModel)
                if (session != null && session.channel != null) {
                    session.channel.writeAndFlush(baseMsgModel);

                    ReceiptModel recModel = new ReceiptModel();
                    recModel.channel = new WeakReference<>(session.channel);
                    recModel.msgModel = baseMsgModel;
                    recModel.isSelf = isSelf;
                    //加入回执缓存
                    SessionHolder.receiptMsg.put(baseMsgModel.msgId + "" + session.clientToken, recModel);
                }
        }
    }

    //TODO 可以优化 如果mq相同 则将to做成数组
    private void sendMsgGroup(BaseMsgModel msgModel) {
        List<SessionModel> seses = SessionHolder.sessionMap.get(msgModel.to);
        for (SessionModel s : seses) {
            s.channel.writeAndFlush(msgModel);

            ReceiptModel recModel = new ReceiptModel();
            recModel.channel = new WeakReference<>(s.channel);
            recModel.msgModel = msgModel;
            recModel.isSelf = msgModel.from.equals(msgModel.to);
            SessionHolder.receiptMsg.put(msgModel.msgId + "" + s.clientToken, recModel);
        }
    }

    private void requestFriend(RequestMsgModel msgModel) {
        FriendModel friendModel = userService.checkFriend(msgModel.from, msgModel.to, true);
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

    private boolean isSelf(String uuid, String self) {
        return uuid.equals(self);
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
