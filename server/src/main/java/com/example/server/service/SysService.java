package com.example.server.service;

import com.example.server.mapper.SysMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysService {
    @Autowired
    public SysMapper sysMapper;

    public long saveUuid(long uuid) {
        return sysMapper.saveUuid(uuid);
    }

    public long getUuid() {
        return sysMapper.getUuid();
    }
}