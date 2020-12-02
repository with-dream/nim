package com.example.server.service;

import com.example.server.mapper.UserMapper;
import com.example.server.entity.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    public UserMapper userMapper;

    public int regist(UserModel userModel) {
        return userMapper.regist(userModel);
    }

    public UserModel sel() {
        return userMapper.sel();
    }
}