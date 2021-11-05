package com.example.server.mapper;

import com.example.server.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import com.example.server.entity.UserCheckEntity;

import java.util.List;

@Repository
public interface UserMapper {
    int register(UserEntity userEntity);

    UserCheckEntity login(UserEntity userEntity);

    int checkUser(String uuid);

    UserEntity userInfo(String uuid);

    FriendEntity checkFriend(FriendEntity friendEntity);

    List<FriendInfoEntity> friendList(@Param(value = "uuid") String uuid);

    boolean isFriend(FriendInfoEntity friendEntity);

    int addFriend(FriendInfoEntity friendEntity);

    int delFriend(FriendInfoEntity friendEntity);

    int blockFriend(FriendInfoEntity friendEntity);

    int delBlockFriend(FriendInfoEntity friendEntity);

}