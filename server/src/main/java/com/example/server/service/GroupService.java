package com.example.server.service;

import com.example.server.entity.*;
import com.example.server.mapper.GroupMapper;
import com.example.server.mapper.UserMapper;
import com.example.server.netty.SendHolder;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utils.L;
import utils.StrUtil;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GroupService {
    @Resource
    public GroupMapper groupMapper;

    @Resource
    SendHolder sendHolder;

    public void init() {
        sendHolder.setGroupMember((groupId) -> {
            Set<String> uuids = new HashSet<>();
            this.getGroupMembers(groupId).forEach(it -> uuids.add(it.uuid));
            return uuids;
        });
    }

    public List<GroupInfoEntity> groupList(String uuid) {
        return groupMapper.groupList(uuid);
    }

    @Transactional
    public int createGroup(String uuid, GroupInfoEntity groupEntity) {
        int res = groupMapper.createGroup(groupEntity);
        GroupMemberEntity memberEntity = new GroupMemberEntity();
        memberEntity.groupId = groupEntity.groupId;
        memberEntity.uuid = uuid;
        memberEntity.role = GroupMemberEntity.OWNER;
        memberEntity.level = 0;
        addMember(memberEntity);
        return res;
    }

    @Transactional
    public int delGroup(String groupId) {
        List<GroupMemberEntity> memList = getGroupMembers(groupId);
        int delCount = groupMapper.delMemberList(memList);
        L.p("delGroup delMemberList delCount==>" + delCount);
        return groupMapper.delGroup(groupId);
    }

    public GroupInfoEntity getGroupInfo(String groupId) {
        return groupMapper.getGroupInfo(groupId);
    }

    public List<GroupMemberEntity> getGroupMembers(String groupId) {
        return groupMapper.getGroupMembers(groupId);
    }

    public int addMember(GroupMemberEntity memberEntity) {
        return groupMapper.addMember(memberEntity);
    }

    public int delMember(GroupMemberEntity memberEntity) {
        return groupMapper.delMember(memberEntity);
    }

    public int delMemberList(List<GroupMemberEntity> memberList) {
        return groupMapper.delMemberList(memberList);
    }

    public List<RequestEntity> getMemberReq(String groupId) {
        return groupMapper.getMemberReq(groupId);
    }

    public int addMemberReq(RequestEntity requestEntity) {
        return groupMapper.addMemberReq(requestEntity);
    }

    public int checkRole(String groupId, String uuid) {
        return groupMapper.checkGroupRole(groupId, uuid);
    }

    public int updateRole(GroupMemberEntity memberEntity) {
        return groupMapper.updateRole(memberEntity);
    }
}