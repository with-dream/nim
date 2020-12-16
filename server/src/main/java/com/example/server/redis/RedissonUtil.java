package com.example.server.redis;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class RedissonUtil {
    private RedissonClient redisClient;
    private static final Object LOCK = new Object();

    public RedissonClient redissonClient() {
        if (this.redisClient != null)
            return this.redisClient;
        synchronized (LOCK) {
            if (this.redisClient == null) {
                Config config = null;
                try {
                    config = Config.fromYAML(new File("application.yml"));
                    this.redisClient = Redisson.create(config);
                    return this.redisClient;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return this.redisClient;
    }

    public RLock getLock(String key) {
        return redissonClient().getLock(key);
    }

    public RReadWriteLock getRWLock(String key) {
        return redissonClient().getReadWriteLock(key);
    }

    public RLock getWLock(String key) {
        RReadWriteLock lock = redissonClient().getReadWriteLock(key);
        return lock.writeLock();
    }

    public RLock getRLock(String key) {
        RReadWriteLock lock = redissonClient().getReadWriteLock(key);
        return lock.writeLock();
    }
}
