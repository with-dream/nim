package com.example.server.service;

import com.example.server.entity.*;
import com.example.server.mapper.UserMapper;
import com.example.server.netty.SendHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import utils.StrUtil;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    @Resource
    public UserMapper userMapper;

    @Resource
    SendHolder sendHolder;

    public void init() {
        sendHolder.setGroupMember((groupId) -> {
            Set<String> uuids = new HashSet<>();
            this.getGroupMembers(groupId).forEach(it -> uuids.add(it.uuid));
            return uuids;
        });
    }

    public int register(UserEntity userEntity) {
        return userMapper.register(userEntity);
    }

    public UserCheckEntity login(UserEntity userEntity) {
        return userMapper.login(userEntity);
    }

    public int checkUser(String uuid) {
        return userMapper.checkUser(uuid);
    }

    public UserEntity userInfo(String uuid) {
        return userMapper.userInfo(uuid);
    }

    public FriendEntity checkFriend(String userId, String friendId) {
        StrUtil.UuidCompare compare = StrUtil.uuidCompare(userId, friendId);
        FriendEntity model = userMapper.checkFriend(compare.low, compare.high);
        if (model == null) return null;

        model.isBlock = model.block == FriendEntity.FRIEND_BLOCK_EACH || model.block == (compare.invert ? FriendEntity.FRIEND_BLOCK_OTHER : FriendEntity.FRIEND_BLOCK_SELF);
        model.isFriend = model.friend == FriendEntity.FRIEND_NORMAL || model.friend == (compare.invert ? FriendEntity.FRIEND_OTHER : FriendEntity.FRIEND_SELF);
        return model;
    }

    public int addFriend(FriendEntity friendEntity) {
        return userMapper.addFriend(friendEntity);
    }

    public int delFriend(FriendEntity friendEntity) {
        return userMapper.delFriend(friendEntity);
    }

    public List<FriendEntity> getAllFriend(String uuid) {
        return userMapper.getAllFriend(uuid);
    }

    public List<GroupInfoEntity> getAllGroup(String uuid) {
        return userMapper.getAllGroup(uuid);
    }

    public GroupInfoEntity getGroupInfo(String groupId) {
        return userMapper.getGroupInfo(groupId);
    }

    //TODO 需要更新群成员数量
    public int addGroupMember(GroupMemberEntity memberEntity) {
        int res = userMapper.addGroupMember(memberEntity);
        return res;
    }

    /**
     * @param type 退群 踢出群
     */
    public int delGroupMember(GroupMemberEntity memberEntity, int type) {
        int role = userMapper.checkGroupRole(memberEntity);
        if (role == GroupMemberEntity.OWNER) {
            return -1;
        }
        int res = 0;
        if (type == 1)
            res = userMapper.delGroupMember(memberEntity);
        return res;
    }

    public List<GroupMemberEntity> getGroupMembers(String groupId) {
        return userMapper.getGroupMembers(groupId);
    }

    @Transactional
    public int createGroup(GroupInfoEntity groupEntity) {
        int res = userMapper.createGroup(groupEntity);

        GroupMemberEntity memberEntity = new GroupMemberEntity();
        memberEntity.groupId = groupEntity.groupId;
        memberEntity.uuid = groupEntity.uuid;
        memberEntity.role = GroupMemberEntity.OWNER;
        memberEntity.level = 0;
        addGroupMember(memberEntity);
        return res;
    }

    /**
     * TODO 需要回滚操作 不完善
     */
    @Transactional
    public int delGroup(GroupInfoEntity groupEntity) {
        List<GroupMemberEntity> memList = getGroupMembers(groupEntity.groupId);
        for (GroupMemberEntity gmm : memList) {
            int res = userMapper.delGroupMember(gmm);
            if (res != 1) {
                return -1;
            }
        }
        return userMapper.delGroup(groupEntity);
    }

    public boolean checkGroup(String groupId) {
        return true;
    }
}