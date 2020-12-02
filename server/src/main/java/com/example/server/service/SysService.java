package com.example.server.service;

import com.example.server.entity.UserModel;
import com.example.server.mapper.SysMapper;
import com.example.server.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysService {
    @Autowired
    public SysMapper sysMapper;

    public int saveUuid(int uuid) {
        return sysMapper.saveUuid(uuid);
    }

    public int getUuid() {
        return sysMapper.getUuid();
    }
}