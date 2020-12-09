package com.example.server.mapper;

import com.example.server.entity.UserModel;
import com.example.server.entity.UserResultModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import user.FriendModel;
import user.GroupMapModel;
import user.GroupModel;

import java.security.acl.Group;
import java.util.List;

@Repository
public interface UserMapper {
    int regist(UserModel userModel);

    UserResultModel login(UserModel userModel);

    int checkUser(long uuid);

    FriendModel checkFriend(@Param(value = "userId") long userId, @Param(value = "friendId") long friendId);

    int addFriend(FriendModel friendModel);

    int delFriend(FriendModel friendModel);

    List<FriendModel> getAllFriend(long uuid);

    int updateGroupMember(GroupModel groupModel);

    GroupModel getGroupInfo(long to);

    int createGroup(GroupModel groupModel);

    int delGroup(GroupModel groupModel);

    int addMapGroup(GroupMapModel groupModel);

    int delMapGroup(@Param(value = "userId") long userId);

    List<GroupModel> getAllGroup(long uuid);
}