package com.example.server.mapper;

import user.GroupInfoModel;
import user.GroupMemberModel;
import user.UserModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import user.FriendModel;

import java.util.List;

@Repository
public interface UserMapper {
    int register(UserModel userModel);

    UserModel login(UserModel userModel);

    int checkUser(String uuid);

    FriendModel checkFriend(@Param(value = "userId") String userId, @Param(value = "friendId") String friendId);

    int addFriend(FriendModel friendModel);

    int delFriend(FriendModel friendModel);

    List<FriendModel> getAllFriend(@Param(value = "uuid") String uuid);

    int addGroupMember(GroupMemberModel memberModel);

    int delGroupMember(GroupMemberModel memberModel);

    List<GroupMemberModel> getGroupMembers(@Param(value = "groupId") long groupId);

    int checkGroupRole(GroupMemberModel memberModel);

    GroupInfoModel getGroupInfo(@Param(value = "groupId") long groupId);

    int createGroup(GroupInfoModel groupModel);

    int delGroup(GroupInfoModel groupModel);

    List<GroupInfoModel> getAllGroup(@Param(value = "uuid") String uuid);
}