package com.example.server.service;

import com.example.server.entity.*;
import com.example.server.mapper.UserMapper;
import com.example.server.netty.SendHolder;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserService {
    @Resource
    public UserMapper userMapper;

    public int register(UserEntity userEntity) {
        return userMapper.register(userEntity);
    }

    public UserResEntity login(UserEntity userEntity) {
        return userMapper.login(userEntity);
    }

    public boolean checkUser(String uuid) {
        int ret = userMapper.checkUser(uuid);
        if (ret > 1) throw new RuntimeException("uuid异常:" + uuid);
        return ret == 1;
    }

    public UserInfoEntity userInfo(String uuid) {
        return userMapper.userInfo(uuid);
    }

    public List<FriendInfoEntity> friendList(String uuid) {
        return userMapper.friendList(uuid);
    }

    public boolean isFriend(FriendInfoEntity friendEntity) {
        return userMapper.isFriend(friendEntity);
    }

    public int addFriendReq(FriendReqEntity reqEntity) {
        return userMapper.addFriendReq(reqEntity);
    }

    public List<FriendReqEntity> friendReqList(String userId) {
        return userMapper.friendReqList(userId);
    }

    public int addFriendAffirm(FriendInfoEntity friendEntity) {
        return userMapper.addFriendAffirm(friendEntity);
    }

    public int delFriend(FriendInfoEntity friendEntity) {
        return userMapper.delFriend(friendEntity);
    }

    public int blockFriend(FriendInfoEntity friendEntity) {
        return blockFriend(friendEntity);
    }

    public int delBlockFriend(FriendInfoEntity friendEntity) {
        return delBlockFriend(friendEntity);
    }

    public boolean isBlock(FriendInfoEntity friendEntity) {
        return userMapper.isBlock(friendEntity);
    }

    public boolean isBlockAny(FriendInfoEntity friendEntity) {
        return userMapper.isBlockAny(friendEntity);
    }

    public int stickFriend(StickEntity stickEntity) {
        return userMapper.stickFriend(stickEntity);
    }

    public List<StickEntity> stickFriendList(String uuid) {
        return userMapper.stickFriendList(uuid);
    }

    public int delStickFriend(StickEntity stickEntity) {
        return userMapper.delStickFriend(stickEntity);
    }

    public int addFriendFolder(FriendFolderEntity folderEntity) {
        return userMapper.addFriendFolder(folderEntity);
    }

    public int delFriendFolder(String uuid, int id) {
        return userMapper.delFriendFolder(uuid, id);
    }

    public List<FriendFolderEntity> friendFolderList(String uuid) {
        return userMapper.friendFolderList(uuid);
    }

    public int updateFriendFolder(List<FriendFolderEntity> folderEntityList) {
        return userMapper.updateFriendFolder(folderEntityList);
    }
}