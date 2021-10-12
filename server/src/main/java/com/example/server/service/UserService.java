package com.example.server.service;

import com.example.server.entity.UserResultModel;
import com.example.server.mapper.UserMapper;
import com.example.server.entity.UserModel;
import com.example.server.netty.SessionHolder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import netty.model.GroupMember;
import netty.model.RequestMsgModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import user.FriendModel;
import user.GroupMapModel;
import user.GroupModel;
import utils.StrUtil;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    @Resource
    public UserMapper userMapper;

    private Gson gson = new Gson();

    public int regist(UserModel userModel) {
        return userMapper.regist(userModel);
    }

    public UserResultModel login(UserModel userModel) {
        return userMapper.login(userModel);
    }

    public int checkUser(String uuid) {
        return userMapper.checkUser(uuid);
    }

    public FriendModel checkFriend(String userId, String friendId, boolean auto) {
        boolean reversal = false;
        if (auto) {
            String[] user = StrUtil.getStr(userId, friendId);
            reversal = userId.equals(user[1]);

            userId = user[0];
            friendId = user[1];
        }

        FriendModel model = userMapper.checkFriend(userId, friendId);
        if (model == null) return null;

        model.isBlock = model.status == FriendModel.FRIEND_BLOCK_EACH || model.status == (reversal ? FriendModel.FRIEND_BLOCK_OTHER : FriendModel.FRIEND_BLOCK_SELF);
        if (!model.isBlock)
            model.isFriend = model.status == FriendModel.FRIEND_NORMAL || model.status == (reversal ? FriendModel.FRIEND_OTHER : FriendModel.FRIEND_SELF);
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

    public List<GroupModel> getAllGroup(String uuid) {
        return userMapper.getAllGroup(uuid);
    }

    public GroupModel getGroupInfo(String groupId) {
        return userMapper.getGroupInfo(groupId);
    }

    public int addGroupMember(RequestMsgModel msgModel) {
        //TODO 群成员不应该保存为json
        synchronized (SessionHolder.getSync(String.valueOf(msgModel.groupId))) {
            GroupModel groupModel = userMapper.getGroupInfo(msgModel.groupId);
            if (groupModel != null) {
                List<GroupMember> members = null;
                if (groupModel.members != null && !groupModel.members.isEmpty()) {
                    members = gson.fromJson(groupModel.members, new TypeToken<List<GroupMember>>() {
                    }.getType());

                    for (GroupMember member : members)
                        if (member.userId == msgModel.to)
                            return 0;
                }
                if (members == null)
                    members = new ArrayList<>();

                //已加入
                GroupMember member = new GroupMember();
                member.userId = msgModel.to;
                members.add(member);
                groupModel.members = gson.toJson(members);
                int res = userMapper.updateGroupMember(groupModel);
                if (res > 0)
                    return 0;
            }
        }

        return -1;
    }

    public GroupModel delGroupMember(RequestMsgModel msgModel) {
        synchronized (SessionHolder.getSync(String.valueOf(msgModel.groupId))) {
            GroupModel groupModel = userMapper.getGroupInfo(msgModel.groupId);
            if (groupModel != null) {
                List<GroupMember> members = groupModel.getMembers(gson);

                boolean resRemove = members.removeIf(it -> it.userId == msgModel.from);
                if (!resRemove)
                    return null;

                groupModel.members = groupModel.memToStr(gson, members);
                int res = userMapper.updateGroupMember(groupModel);
                if (res > 0)
                    return groupModel;
            }
        }

        return null;
    }

    public int createGroup(GroupModel groupModel) {
        int res = userMapper.createGroup(groupModel);

        GroupMapModel groupMapModel = new GroupMapModel();
        groupMapModel.userId = groupModel.userId;
        groupMapModel.groupId = groupModel.groupId;
        res = userMapper.addMapGroup(groupMapModel);
        return res;
    }

    public int delGroup(GroupModel groupModel) {
        return userMapper.delGroup(groupModel);
    }

    public int addMapGroup(GroupMapModel groupModel) {
        return userMapper.addMapGroup(groupModel);
    }

    public int delMapGroup(String userId) {
        return userMapper.delMapGroup(userId);
    }

    //TODO 检查群是否存在
    public int checkGroup(String groupId) {
        return 1;
    }
}