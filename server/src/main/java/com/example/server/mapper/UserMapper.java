package com.example.server.mapper;

import com.example.server.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {
    int register(UserEntity userEntity);

    UserCheckEntity login(UserEntity userEntity);

    int checkUser(String uuid);

    FriendEntity checkFriend(@Param(value = "userId") String userId, @Param(value = "friendId") String friendId);

    int addFriend(FriendEntity friendEntity);

    int delFriend(FriendEntity friendEntity);

    List<FriendEntity> getAllFriend(@Param(value = "uuid") String uuid);

    int addGroupMember(GroupMemberEntity memberEntity);

    int delGroupMember(GroupMemberEntity memberEntity);

    List<GroupMemberEntity> getGroupMembers(@Param(value = "groupId") String groupId);

    int checkGroupRole(GroupMemberEntity memberEntity);

    GroupInfoEntity getGroupInfo(@Param(value = "groupId") String groupId);

    int createGroup(GroupInfoEntity groupEntity);

    int delGroup(GroupInfoEntity groupEntity);

    List<GroupInfoEntity> getAllGroup(@Param(value = "uuid") String uuid);

    UserEntity userInfo(@Param(value = "uuid") String uuid);
}