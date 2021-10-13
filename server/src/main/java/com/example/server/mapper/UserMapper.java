package com.example.server.mapper;

import com.example.server.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {
    int register(UserModel userModel);

    UserCheckModel login(UserModel userModel);

    int checkUser(String uuid);

    FriendModel checkFriend(@Param(value = "userId") String userId, @Param(value = "friendId") String friendId);

    int addFriend(FriendModel friendModel);

    int delFriend(FriendModel friendModel);

    List<FriendModel> getAllFriend(@Param(value = "uuid") String uuid);

    int addGroupMember(GroupMemberModel memberModel);

    int delGroupMember(GroupMemberModel memberModel);

    List<GroupMemberModel> getGroupMembers(@Param(value = "groupId") String groupId);

    int checkGroupRole(GroupMemberModel memberModel);

    GroupInfoModel getGroupInfo(@Param(value = "groupId") String groupId);

    int createGroup(GroupInfoModel groupModel);

    int delGroup(GroupInfoModel groupModel);

    List<GroupInfoModel> getAllGroup(@Param(value = "uuid") String uuid);
}