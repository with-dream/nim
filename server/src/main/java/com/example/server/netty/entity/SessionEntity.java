package com.example.server.netty.entity;

import entity.Entity;
import io.netty.channel.Channel;

import java.util.Objects;

public class SessionEntity extends Entity {
    public SessionRedisEntity redisEntity;
    public Channel channel;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionEntity that = (SessionEntity) o;
        return Objects.equals(redisEntity, that.redisEntity) &&
                Objects.equals(channel, that.channel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(redisEntity, channel);
    }

    @Override
    public String toString() {
        return "SessionEntity{" +
                "redisEntity=" + redisEntity +
                '}';
    }
}
