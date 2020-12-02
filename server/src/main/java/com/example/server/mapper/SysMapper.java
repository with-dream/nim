package com.example.server.mapper;

import org.springframework.stereotype.Repository;

@Repository
public interface SysMapper {
    int saveUuid(int uuid);
    int getUuid();
}