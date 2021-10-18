package com.example.server.netty;

import entity.Entity;

public class SessionRedisModel extends Entity {
    public String uuid;
    public int clientToken;
    public int deviceType;
    public String queueName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionRedisModel that = (SessionRedisModel) o;
        return that.uuid.equals(uuid) && that.clientToken == clientToken
                && that.deviceType == deviceType;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "SessionRedisModel{" +
                "queueName='" + queueName + '\'' +
                ", uuid='" + uuid + '\'' +
                ", clientToken=" + clientToken +
                ", deviceType=" + deviceType +
                '}';
    }
}
