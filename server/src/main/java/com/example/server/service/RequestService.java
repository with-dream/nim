package com.example.server.service;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RequestService {

    private static RequestService that;

    @PostConstruct
    public void init() {
        that = this;
    }
//
//    @Resource
//    public UserService userService;
//
//    @Resource
//    public SendHolder sendHolder;
//
//    public boolean requestMsg(NimMsg msg, Channel channel) {
//        int cmd = NullUtil.isInt(msg.msgMap().get(MsgType.KEY_CMD));
//        switch (cmd) {
//            case MsgReq.REQUEST_FRIEND:
//                requestFriend(msg, channel);
//                break;
//            case MsgReq.GROUP_ADD:
//                GroupInfoModel group = that.userService.getGroupInfo(msg.groupId);
//                //TODO 申请群 将目标群指向为群拥有者和群管理 待完善
//                msg.to = group.uuid;
//                that.holder.sendMsq(msg, channel, TagList.TAG_REQ, true);
//                //TODO 申请群时 需要记录申请状态 保存在mysql中
//                break;
//            case MsgReq.GROUP_EXIT:
//                delGroupMember(msg);
//                break;
//            case MsgReq.GROUP_DEL:
//                delGroup(msg);
//                break;
//            case MsgReq.GROUP_ADD_AGREE:
//                addGroupMember(msg, channel);
//                break;
//            case MsgReq.REQUEST_FRIEND_AGREE:
//                FriendModel friendModel = new FriendModel();
//                String[] user = StrUtil.getStr(msg.from, msg.to);
//                friendModel.userId = user[0];
//                friendModel.friendId = user[1];
//                friendModel.status = 1;
//                int res = that.userService.addFriend(friendModel);
//                if (res > 0) {
////                    writeReqMsg(msg);
//                }
//                break;
//            case MsgReq.FRIEND_DEL:
//            case MsgReq.FRIEND_DEL_EACH:
//            case MsgReq.FRIEND_DEL_BLOCK:
//            case MsgReq.FRIEND_DEL_UNBLOCK:
//                FriendModel delModel = new FriendModel();
//                FriendModel resCheck = that.userService.checkFriend(delModel.userId, delModel.friendId, true);
//                //如果是双向好友
//                if (resCheck.status == FriendModel.FRIEND_NORMAL) {
//                    if (MsgReq.FRIEND_DEL == msg.cmd)
//                        delModel.status = FriendModel.FRIEND_OTHER;
//                    else if (MsgReq.FRIEND_DEL_EACH == msg.cmd)
//                        delModel.status = FriendModel.FRIEND_DEL_EACH;
//                    //如果是单向好友
//                } else if (resCheck.status == FriendModel.FRIEND_SELF)
//                    delModel.status = FriendModel.FRIEND_DEL_EACH;
//                    //拉黑操作
//                else if (MsgReq.FRIEND_DEL_BLOCK == msg.cmd) {
//                    if (resCheck.status == FriendModel.FRIEND_BLOCK_OTHER)
//                        delModel.status = FriendModel.FRIEND_BLOCK_EACH;
//                    else if (resCheck.status != FriendModel.FRIEND_BLOCK_EACH)
//                        delModel.status = FriendModel.FRIEND_SELF;
//                    //解除拉黑 解除拉黑后 为删除好友的状态
//                } else if (MsgReq.FRIEND_DEL_UNBLOCK == msg.cmd) {
//                    if (resCheck.status == FriendModel.FRIEND_BLOCK_EACH)
//                        delModel.status = FriendModel.FRIEND_OTHER;
//                    else if (resCheck.status == FriendModel.FRIEND_BLOCK_SELF)
//                        delModel.status = FriendModel.FRIEND_DEL_EACH;
//                }
//
//                int delRes = that.userService.delFriend(delModel);
//                if (delRes > 0) {
//                    msg.status = delModel.status;
////                    writeReqMsg(reqMsg);
//                }
//                break;
//        }
//
//        return true;
//    }
//
//    private int requestFriend(NimMsg msg, Channel channel) {
//        FriendModel friendModel = that.userService.checkFriend(msg.from, msg.to);
//        //空表示不是好友 且没有被拉黑
//        if (friendModel == null || (!friendModel.isFriend && !friendModel.isBlock)) {
//            that.holder.sendMsq(msgModel, channel, TagList.TAG_REQ, true);
//            return;
//        }
//    }
//
//    //加入群
//    private void addGroupMember(MsgReq msgModel, Channel channel) {
//        GroupMemberModel memberModel = new GroupMemberModel();
//        memberModel.groupId = msgModel.groupId;
////        memberModel.uuid = msgModel.
//        memberModel.level = 0;
//        memberModel.role = GroupMemberModel.MEMBER;
//
//        int res = that.userService.addGroupMember(memberModel);
//        if (res == 1) {
//            that.holder.sendMsq(msgModel, channel, TagList.TAG_REQ, false);
//        } else
//            L.e("加入群错误");
//    }
//
//    //TODO 退群 sql不合理
//    private void delGroupMember(MsgReq msgModel) {
//
//    }
//
//    //TODO 退群 sql不合理
//    //解散群
//    private void delGroup(MsgReq msgModel) {
//
//    }
}
