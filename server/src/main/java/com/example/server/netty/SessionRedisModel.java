package com.example.server.netty;

public class SessionRedisModel extends SessionBase {
    public String queueName;

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
