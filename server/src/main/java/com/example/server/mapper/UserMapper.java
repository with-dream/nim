package com.example.server.mapper;

import com.example.server.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface UserMapper {
    int register(UserEntity userEntity);

    UserResEntity login(UserEntity userEntity);

    int checkUser(@Param(value = "uuid") String uuid);

    UserInfoEntity userInfo(@Param(value = "uuid") String uuid);

    List<FriendInfoEntity> friendList(@Param(value = "uuid") String uuid);

    boolean isFriend(FriendInfoEntity friendEntity);

    int addFriendReq(RequestEntity reqEntity);

    List<RequestEntity> friendReqList(@Param(value = "userId") String userId);

    int addFriendAffirm(FriendInfoEntity friendEntity);

    int delFriend(@Param(value = "userId") String userId, @Param(value = "friendId") String friendId, @Param(value = "insertTime") Date insertTime, @Param(value = "each") int each);

    int blockFriend(FriendInfoEntity friendEntity);

    int delBlockFriend(FriendInfoEntity friendEntity);

    boolean isBlock(FriendInfoEntity friendEntity);

    boolean isBlockAny(FriendInfoEntity friendEntity);

    int stickFriend(StickEntity stickEntity);

    List<StickEntity> stickFriendList(@Param(value = "uuid") String uuid);

    int delStickFriend(StickEntity stickEntity);

    int addFriendFolder(FriendFolderEntity folderEntity);

    int delFriendFolder(@Param(value = "uuid") String uuid, @Param(value = "id") int id);

    List<FriendFolderEntity> friendFolderList(@Param(value = "uuid") String uuid);

    @Transactional
    int updateFriendFolder(@Param(value = "folderList") List<FriendFolderEntity> folderEntityList);
}