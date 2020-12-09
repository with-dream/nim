package com.example.server.mapper;

import org.springframework.stereotype.Repository;

@Repository
public interface SysMapper {
    long saveUuid(long uuid);
    long getUuid();
}