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

    GroupInfoEntity getGroupInfo(@Param(value = "groupId") String groupId);

    List<GroupMemberEntity> getGroupMembers(@Param(value = "groupId") String groupId);

    int addMember(GroupMemberEntity memberEntity);

    int delMember(GroupMemberEntity memberEntity);

    RequestEntity getMemberReq(@Param(value = "groupId") String groupId);

    int addMemberReq(RequestEntity requestEntity);

    int checkGroupRole(@Param(value = "groupId") String groupId, @Param(value = "uuid") String uuid);

    int updateRole(@Param(value = "groupId") String groupId, @Param(value = "uuid") String uuid);
}