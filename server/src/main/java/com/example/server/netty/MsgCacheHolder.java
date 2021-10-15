package com.example.server.netty;

import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

public class MsgCacheHolder {
    @Resource
    public RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存消息
     * */
    public boolean cacheMsg() {

    }

    /**
     * 保存离线消息 群消息不能保存为离线消息
     * */
    public boolean saveOfflineMsg() {

    }
}
