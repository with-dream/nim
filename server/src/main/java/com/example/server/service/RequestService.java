package com.example.server.service;

import com.example.server.entity.FriendModel;
import com.example.server.netty.SendHolder;
import com.example.server.netty.MsgBuild;
import netty.entity.MsgReq;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import org.springframework.stereotype.Component;
import utils.NullUtil;
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
    public SendHolder sendHolder;

    public boolean requestMsg(NimMsg msg) {
        int cmd = NullUtil.isInt(msg.msgMap().get(MsgType.KEY_CMD));
        switch (cmd) {
            case MsgReq.REQUEST_FRIEND:
                requestFriend(msg);
                break;
            case MsgReq.GROUP_ADD:

                break;
            case MsgReq.GROUP_EXIT:

                break;
            case MsgReq.GROUP_DEL:

                break;
            case MsgReq.GROUP_ADD_AGREE:

                break;
            case MsgReq.REQUEST_FRIEND_AGREE:
                FriendModel friendModel = new FriendModel();
                StrUtil.UuidCompare compare = StrUtil.uuidCompare(msg.to, msg.from);
                friendModel.userId = compare.low;
                friendModel.friendId = compare.high;
                friendModel.friend = FriendModel.FRIEND_NORMAL;
                int res = that.userService.addFriend(friendModel);
                if (res > 0) {
                    that.sendHolder.sendMsg(msg);
                }
                break;
            case MsgReq.FRIEND_DEL:
            case MsgReq.FRIEND_DEL_EACH:
            case MsgReq.FRIEND_DEL_BLOCK:
            case MsgReq.FRIEND_DEL_UNBLOCK:
                FriendModel delModel = new FriendModel();
                FriendModel resCheck = that.userService.checkFriend(msg.from, msg.to);
                int msgReq = NullUtil.isInt(msg.msgMap().get(MsgType.KEY_CMD));
                //如果是双向好友
                if (resCheck.friend == FriendModel.FRIEND_NORMAL) {
                    if (MsgReq.FRIEND_DEL == msgReq)
                        delModel.friend = FriendModel.FRIEND_OTHER;
                    else if (MsgReq.FRIEND_DEL_EACH == msgReq)
                        delModel.friend = FriendModel.FRIEND_DEL_EACH;
                    //如果是单向好友
                } else if (resCheck.friend == FriendModel.FRIEND_SELF)
                    delModel.friend = FriendModel.FRIEND_DEL_EACH;
                    //拉黑操作
                else if (MsgReq.FRIEND_DEL_BLOCK == msgReq) {
                    if (resCheck.block == FriendModel.FRIEND_BLOCK_OTHER)
                        delModel.block = FriendModel.FRIEND_BLOCK_EACH;
                    else if (resCheck.block != FriendModel.FRIEND_BLOCK_EACH)
                        delModel.friend = FriendModel.FRIEND_SELF;
                    //解除拉黑 解除拉黑后 为删除好友的状态
                } else if (MsgReq.FRIEND_DEL_UNBLOCK == msgReq) {
                    if (resCheck.block == FriendModel.FRIEND_BLOCK_EACH)
                        delModel.friend = FriendModel.FRIEND_OTHER;
                    else if (resCheck.block == FriendModel.FRIEND_BLOCK_SELF)
                        delModel.friend = FriendModel.FRIEND_DEL_EACH;
                }

                int delRes = that.userService.delFriend(delModel);
                if (delRes > 0) {
                    that.sendHolder.sendMsg(msg);
                }
                break;
        }

        return true;
    }

    private int requestFriend(NimMsg msg) {
        FriendModel friendModel = that.userService.checkFriend(msg.from, msg.to);
        if (friendModel == null || !friendModel.isFriend) {
            //TODO 检查添加权限
//            UserModel user =that.userService.userInfo(msg.to);

            that.sendHolder.sendMsg(msg);
            return 0;
        }
        //如果已经被拉黑 则直接返回
        if (friendModel.isBlock || friendModel.isFriend) {
            NimMsg recMsg = MsgBuild.recMsg(msg);
            int recCode = 0;
            if (friendModel.isBlock)
                recCode = MsgReq.REQUEST_FRIEND_BLOCK;
            else if (friendModel.isFriend)
                recCode = MsgReq.REQUEST_FRIEND_FRIEND;

            recMsg.msgMap().put(MsgType.KEY_RECEIPT_EXTRA_CODE, recCode);
            that.sendHolder.sendMsg(recMsg);
            return 0;
        }
        return 0;
    }
}
