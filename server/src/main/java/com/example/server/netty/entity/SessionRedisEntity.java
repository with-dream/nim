package com.example.server.netty.entity;

import entity.Entity;

public class SessionRedisEntity extends Entity {
    public String uuid;
    public long clientToken;
    public int deviceType;
    public String queueName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionRedisEntity that = (SessionRedisEntity) o;
        return that.uuid.equals(uuid) && that.clientToken == clientToken
                && that.deviceType == deviceType;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "SessionRedisEntity{" +
                "queueName='" + queueName + '\'' +
                ", uuid='" + uuid + '\'' +
                ", clientToken=" + clientToken +
                ", deviceType=" + deviceType +
                '}';
    }
}
