package com.example.server.service;

import com.example.server.entity.*;
import com.example.server.mapper.UserMapper;
import com.example.server.netty.SendHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import com.example.server.entity.UserCheckEntity;
import utils.StrUtil;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    @Resource
    public UserMapper userMapper;

    @Resource
    SendHolder sendHolder;

    public int register(UserEntity userEntity) {
        return userMapper.register(userEntity);
    }

    public UserCheckEntity login(UserEntity userEntity) {
        return userMapper.login(userEntity);
    }

    public int checkUser(String uuid) {
        return userMapper.checkUser(uuid);
    }

    public UserEntity userInfo(String uuid) {
        return userMapper.userInfo(uuid);
    }

    public List<FriendInfoEntity> friendList(String uuid) {
        return userMapper.friendList(uuid);
    }

    boolean isFriend(FriendInfoEntity friendEntity) {
        return userMapper.isFriend(friendEntity);
    }

    public int addFriend(FriendInfoEntity friendEntity) {
        return userMapper.addFriend(friendEntity);
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
}