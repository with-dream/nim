package com.example.server.service;

import com.example.server.entity.FriendEntity;
import com.example.server.netty.SendHolder;
import com.example.server.netty.MsgBuild;
import netty.entity.MsgCmd;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import org.springframework.stereotype.Component;
import utils.NullUtil;
import utils.StrUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

//1000-3000
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

    public int requestMsg(NimMsg msg) {
        int ret = MsgType.STATE_RECEIPT_SERVER_SUCCESS;
        int cmd = NullUtil.isInt(msg.msgMap().get(MsgType.KEY_CMD));
        switch (cmd) {
            case MsgCmd.REQUEST_FRIEND:
                ret = requestFriend(msg);
                break;
            case MsgCmd.REQUEST_FRIEND_AGREE:
                FriendEntity friendEntity = new FriendEntity();
                StrUtil.UuidCompare compare = StrUtil.uuidCompare(msg.to, msg.from);
                friendEntity.userId = compare.low;
                friendEntity.friendId = compare.high;
                friendEntity.friend = FriendEntity.FRIEND_NORMAL;
                int res = that.userService.addFriend(friendEntity);
                if (res > 0) {
                    ret = that.sendHolder.sendMsg(msg);
                } else {

                }
                break;
            case MsgCmd.FRIEND_DEL:
            case MsgCmd.FRIEND_DEL_EACH:
            case MsgCmd.FRIEND_DEL_BLOCK:
            case MsgCmd.FRIEND_DEL_UNBLOCK:
                FriendEntity delEntity = new FriendEntity();
                FriendEntity resCheck = that.userService.checkFriend(msg.from, msg.to);
                int msgReq = NullUtil.isInt(msg.msgMap().get(MsgType.KEY_CMD));
                //如果是双向好友
                if (resCheck.friend == FriendEntity.FRIEND_NORMAL) {
                    if (MsgCmd.FRIEND_DEL == msgReq)
                        delEntity.friend = FriendEntity.FRIEND_OTHER;
                    else if (MsgCmd.FRIEND_DEL_EACH == msgReq)
                        delEntity.friend = FriendEntity.FRIEND_DEL_EACH;
                    //如果是单向好友
                } else if (resCheck.friend == FriendEntity.FRIEND_SELF)
                    delEntity.friend = FriendEntity.FRIEND_DEL_EACH;
                    //拉黑操作
                else if (MsgCmd.FRIEND_DEL_BLOCK == msgReq) {
                    if (resCheck.block == FriendEntity.FRIEND_BLOCK_OTHER)
                        delEntity.block = FriendEntity.FRIEND_BLOCK_EACH;
                    else if (resCheck.block != FriendEntity.FRIEND_BLOCK_EACH)
                        delEntity.friend = FriendEntity.FRIEND_SELF;
                    //解除拉黑 解除拉黑后 为删除好友的状态
                } else if (MsgCmd.FRIEND_DEL_UNBLOCK == msgReq) {
                    if (resCheck.block == FriendEntity.FRIEND_BLOCK_EACH)
                        delEntity.friend = FriendEntity.FRIEND_OTHER;
                    else if (resCheck.block == FriendEntity.FRIEND_BLOCK_SELF)
                        delEntity.friend = FriendEntity.FRIEND_DEL_EACH;
                }

                int delRes = that.userService.delFriend(delEntity);
                if (delRes > 0) {
                    ret = that.sendHolder.sendMsg(msg);
                }
                break;

            case MsgCmd.GROUP_ADD:

                break;
            case MsgCmd.GROUP_EXIT:

                break;
            case MsgCmd.GROUP_DEL:

                break;
            case MsgCmd.GROUP_ADD_AGREE:

                break;
        }

        return ret;
    }

    private int requestFriend(NimMsg msg) {
        FriendEntity friendEntity = that.userService.checkFriend(msg.from, msg.to);
        if (friendEntity == null || !friendEntity.isFriend) {
            //TODO 检查添加权限
//            UserEntity user =that.userService.userInfo(msg.to);


            return that.sendHolder.sendMsg(msg);
        }
        //如果已经被拉黑 则直接返回
        if (friendEntity.isBlock || friendEntity.isFriend) {
            NimMsg recMsg = MsgBuild.recMsg(msg);
            int recCode = 0;
            if (friendEntity.isBlock)
                recCode = MsgCmd.REQUEST_FRIEND_BLOCK;
            else if (friendEntity.isFriend)
                recCode = MsgCmd.REQUEST_FRIEND_FRIEND;

            recMsg.msgMap().put(MsgType.KEY_RECEIPT_EXTRA_CODE, recCode);

            return that.sendHolder.sendMsg(recMsg);
        }
        return 0;
    }
}
