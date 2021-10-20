package com.example.server.service;

import com.example.server.entity.*;
import com.example.server.mapper.UserMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import utils.StrUtil;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserService {
    @Resource
    public UserMapper userMapper;

    public int register(UserModel userModel) {
        return userMapper.register(userModel);
    }

    public UserCheckModel login(UserModel userModel) {
        return userMapper.login(userModel);
    }

    public int checkUser(String uuid) {
        return userMapper.checkUser(uuid);
    }

    public UserModel userInfo(String uuid) {
        return userMapper.userInfo(uuid);
    }

    public FriendModel checkFriend(String userId, String friendId) {
        StrUtil.UuidCompare compare = StrUtil.uuidCompare(userId, friendId);
        FriendModel model = userMapper.checkFriend(compare.low, compare.high);
        if (model == null) return null;

        model.isBlock = model.block == FriendModel.FRIEND_BLOCK_EACH || model.block == (compare.invert ? FriendModel.FRIEND_BLOCK_OTHER : FriendModel.FRIEND_BLOCK_SELF);
        model.isFriend = model.friend == FriendModel.FRIEND_NORMAL || model.friend == (compare.invert ? FriendModel.FRIEND_OTHER : FriendModel.FRIEND_SELF);
        return model;
    }

    public int addFriend(FriendModel friendModel) {
        return userMapper.addFriend(friendModel);
    }

    public int delFriend(FriendModel friendModel) {
        return userMapper.delFriend(friendModel);
    }

    public List<FriendModel> getAllFriend(String uuid) {
        return userMapper.getAllFriend(uuid);
    }

    public List<GroupInfoModel> getAllGroup(String uuid) {
        return userMapper.getAllGroup(uuid);
    }

    public GroupInfoModel getGroupInfo(String groupId) {
        return userMapper.getGroupInfo(groupId);
    }

    //TODO 需要更新群成员数量
    public int addGroupMember(GroupMemberModel memberModel) {
        int res = userMapper.addGroupMember(memberModel);
        return res;
    }

    /**
     * @param type 退群 踢出群
     */
    public int delGroupMember(GroupMemberModel memberModel, int type) {
        int role = userMapper.checkGroupRole(memberModel);
        if (role == GroupMemberModel.OWNER) {
            return -1;
        }
        int res = 0;
        if (type == 1)
            res = userMapper.delGroupMember(memberModel);
        return res;
    }

    public List<GroupMemberModel> getGroupMembers(String groupId) {
        return userMapper.getGroupMembers(groupId);
    }

    @Transactional
    public int createGroup(GroupInfoModel groupModel) {
        int res = userMapper.createGroup(groupModel);

        GroupMemberModel memberModel = new GroupMemberModel();
        memberModel.groupId = groupModel.groupId;
        memberModel.uuid = groupModel.uuid;
        memberModel.role = GroupMemberModel.OWNER;
        memberModel.level = 0;
        addGroupMember(memberModel);
        return res;
    }

    /**
     * TODO 需要回滚操作 不完善
     */
    @Transactional
    public int delGroup(GroupInfoModel groupModel) {
        List<GroupMemberModel> memList = getGroupMembers(groupModel.groupId);
        for (GroupMemberModel gmm : memList) {
            int res = userMapper.delGroupMember(gmm);
            if (res != 1) {
                return -1;
            }
        }
        return userMapper.delGroup(groupModel);
    }

    public boolean checkGroup(String groupId) {
        return true;
    }
}