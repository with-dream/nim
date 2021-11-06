package com.example.server.mapper;

import com.example.server.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMapper {
    List<GroupInfoEntity> groupList(@Param(value = "uuid") String uuid);

    int createGroup(GroupInfoEntity groupEntity);

    int delGroup(@Param(value = "groupId") String groupId);

    int addGroupMember(GroupMemberEntity memberEntity);

    int delGroupMember(GroupMemberEntity memberEntity);

    List<GroupMemberEntity> getGroupMembers(@Param(value = "groupId") String groupId);

    int checkGroupRole(GroupMemberEntity memberEntity);

    GroupInfoEntity getGroupInfo(@Param(value = "groupId") String groupId);

    UserEntity userInfo(@Param(value = "uuid") String uuid);
}