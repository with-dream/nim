package com.example.server.service;

import com.example.server.ApplicationRunnerImpl;
import com.example.server.entity.GroupMsgModel;
import com.example.server.netty.NettyServerHandler;
import com.example.server.netty.SessionHolder;
import com.example.server.netty.SessionModel;
import com.example.server.netty.SessionServerHolder;
import com.example.server.redis.TagList;
import io.netty.channel.Channel;
import netty.MQWrapper;
import netty.model.BaseMsgModel;
import netty.model.MsgType;
import netty.model.ReceiptModel;
import netty.model.RequestMsgModel;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import user.FriendModel;
import user.GroupMapModel;
import user.GroupModel;
import utils.L;
import utils.StrUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class RequestService {

    private static RequestService that;

    @PostConstruct
    public void init() {
        that = this;
    }

    @Resource
    public UserService userService;

    @Resource
    public SessionServerHolder holder;

    public boolean requestMsg(RequestMsgModel reqMsg, Channel channel) {
        switch (reqMsg.cmd) {
            case RequestMsgModel.REQUEST_FRIEND:
                requestFriend(reqMsg, channel);
                break;
            case RequestMsgModel.GROUP_ADD:
                GroupModel group = that.userService.getGroupInfo(reqMsg.groupId);
                //TODO 申请群 将目标群指向为群拥有者和群管理 待完善
                reqMsg.to = group.userId;
                that.holder.sendMsq(reqMsg, channel, TagList.TAG_REQ, true);
                //TODO 申请群时 需要记录申请状态 保存在mysql中
                break;
            case RequestMsgModel.GROUP_EXIT:
                delGroupMember(reqMsg);
                break;
            case RequestMsgModel.GROUP_DEL:
                delGroup(reqMsg);
                break;
            case RequestMsgModel.GROUP_ADD_AGREE:
                addGroupMember(reqMsg, channel);
                break;
            case RequestMsgModel.REQUEST_FRIEND_AGREE:
                FriendModel friendModel = new FriendModel();
                String[] user = StrUtil.getStr(reqMsg.from, reqMsg.to);
                friendModel.userId = user[0];
                friendModel.friendId = user[1];
                friendModel.status = 1;
                int res = that.userService.addFriend(friendModel);
                if (res > 0) {
//                    writeReqMsg(reqMsg);
                }
                break;
            case RequestMsgModel.FRIEND_DEL:
            case RequestMsgModel.FRIEND_DEL_EACH:
            case RequestMsgModel.FRIEND_DEL_BLOCK:
            case RequestMsgModel.FRIEND_DEL_UNBLOCK:
                FriendModel delModel = new FriendModel();
                FriendModel resCheck = that.userService.checkFriend(delModel.userId, delModel.friendId, true);
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

                int delRes = that.userService.delFriend(delModel);
                if (delRes > 0) {
                    reqMsg.status = delModel.status;
//                    writeReqMsg(reqMsg);
                }
                break;
        }

        return true;
    }

    private void requestFriend(RequestMsgModel msgModel, Channel channel) {
        FriendModel friendModel = that.userService.checkFriend(msgModel.from, msgModel.to, true);
        //空表示不是好友 且没有被拉黑
        if (friendModel == null || (!friendModel.isFriend && !friendModel.isBlock)) {
            that.holder.sendMsq(msgModel, channel, TagList.TAG_REQ, true);
            return;
        }
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
            that.holder.sendMsq(msgModel, channel, TagList.TAG_REQ, false);
        } else
            L.e("加入群错误");
    }

    //TODO 退群 sql不合理
    private void delGroupMember(RequestMsgModel msgModel) {
        GroupModel res = that.userService.delGroupMember(msgModel);
        if (res != null) {
            int resDel = that.userService.delMapGroup(msgModel.from);
            if (resDel != 1)
                L.e("delGroupMember==>失败");

            GroupMsgModel groupMsgModel = GroupMsgModel.createG(msgModel.from, msgModel.groupId);
            groupMsgModel.fromToken = msgModel.fromToken;
            groupMsgModel.deviceType = msgModel.deviceType;
            groupMsgModel.cmd = RequestMsgModel.GROUP_EXIT;
            that.holder.sendGroupMsq(groupMsgModel);
        } else
            L.e("退群错误");
    }

    //TODO 退群 sql不合理
    //解散群
    private void delGroup(RequestMsgModel msgModel) {
        GroupModel res = that.userService.delGroupMember(msgModel);
        if (res != null) {
            int resDel = that.userService.delMapGroup(msgModel.from);
            if (resDel != 1)
                L.e("delGroupMember==>失败");

            GroupMsgModel groupMsgModel = GroupMsgModel.createG(msgModel.from, msgModel.groupId);
            groupMsgModel.fromToken = msgModel.fromToken;
            groupMsgModel.deviceType = msgModel.deviceType;
            groupMsgModel.cmd = RequestMsgModel.GROUP_DEL;
            that.holder.sendGroupMsq(groupMsgModel);
        } else
            L.e("退群错误");
    }

    private boolean checkSelf(int token, BaseMsgModel msg) {
        return token == msg.fromToken;
    }
}
