package com.example.server.netty;

import entity.Entity;

public class SessionBase extends Entity {
    public String uuid;
    public int clientToken;
    public int deviceType;

    @Override
    public String toString() {
        return "SessionBase{" +
                "uuid='" + uuid + '\'' +
                ", clientToken=" + clientToken +
                ", deviceType=" + deviceType +
                '}';
    }
}
