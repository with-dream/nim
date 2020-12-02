package com.example.server.mapper;

import com.example.server.entity.UserModel;
import com.example.server.entity.UserResultModel;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {
    int regist(UserModel userModel);
    UserResultModel login(UserModel userModel);
    int checkUser(long uuid);
}