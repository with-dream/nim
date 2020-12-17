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

    int checkUser(String uuid);

    FriendModel checkFriend(@Param(value = "userId") String userId, @Param(value = "friendId") String friendId);

    int addFriend(FriendModel friendModel);

    int delFriend(FriendModel friendModel);

    List<FriendModel> getAllFriend(String uuid);

    int updateGroupMember(GroupModel groupModel);

    GroupModel getGroupInfo(String to);

    int createGroup(GroupModel groupModel);

    int delGroup(GroupModel groupModel);

    int addMapGroup(GroupMapModel groupModel);

    int delMapGroup(@Param(value = "userId") String userId);

    List<GroupModel> getAllGroup(String uuid);
}