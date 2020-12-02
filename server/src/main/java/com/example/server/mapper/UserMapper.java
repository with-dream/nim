package com.example.server.mapper;

import com.example.server.entity.UserModel;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {
    int regist(UserModel userModel);

    UserModel sel();
}