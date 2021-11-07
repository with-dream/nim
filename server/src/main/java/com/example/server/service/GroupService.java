package com.example.server.service;

import com.example.server.entity.*;
import com.example.server.mapper.GroupMapper;
import com.example.server.mapper.UserMapper;
import com.example.server.netty.SendHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        addGroupMember(memberEntity);
        return res;
    }

    //TODO 需要更新群成员数量
    public int addGroupMember(GroupMemberEntity memberEntity) {
        int res = groupMapper.addGroupMember(memberEntity);
        return res;
    }

    /**
     * @param type 退群 踢出群
     */
    public int delGroupMember(GroupMemberEntity memberEntity, int type) {
        int role = groupMapper.checkGroupRole(memberEntity);
        if (role == GroupMemberEntity.OWNER) {
            return -1;
        }
        int res = 0;
        if (type == 1)
            res = groupMapper.delGroupMember(memberEntity);
        return res;
    }

    public List<GroupMemberEntity> getGroupMembers(String groupId) {
        return groupMapper.getGroupMembers(groupId);
    }

    /**
     * TODO 需要回滚操作 不完善
     */
    @Transactional
    public int delGroup(GroupInfoEntity groupEntity) {
        List<GroupMemberEntity> memList = getGroupMembers(groupEntity.groupId);
        for (GroupMemberEntity gmm : memList) {
            int res = groupMapper.delGroupMember(gmm);
            if (res != 1) {
                return -1;
            }
        }
        return groupMapper.delGroup(groupEntity);
    }

    public boolean checkGroup(String groupId) {
        return true;
    }
}