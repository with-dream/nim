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

    public FriendEntity checkFriend(String userId, String friendId) {
        StrUtil.UuidCompare compare = StrUtil.uuidCompare(userId, friendId);
        FriendEntity model = userMapper.checkFriend(compare.low, compare.high);
        if (model == null) return null;

        model.isBlock = model.block == FriendEntity.FRIEND_BLOCK_EACH || model.block == (compare.invert ? FriendEntity.FRIEND_BLOCK_OTHER : FriendEntity.FRIEND_BLOCK_SELF);
        model.isFriend = model.friend == FriendEntity.FRIEND_NORMAL || model.friend == (compare.invert ? FriendEntity.FRIEND_OTHER : FriendEntity.FRIEND_SELF);
        return model;
    }

    public int addFriend(FriendEntity friendEntity) {
        return userMapper.addFriend(friendEntity);
    }

    public int delFriend(FriendEntity friendEntity) {
        return userMapper.delFriend(friendEntity);
    }

    public List<FriendEntity> getAllFriend(String uuid) {
        return userMapper.getAllFriend(uuid);
    }
}