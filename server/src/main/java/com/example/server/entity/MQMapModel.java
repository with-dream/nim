package com.example.server.entity;

import netty.model.DeviceType;

import java.io.Serializable;

public class MQMapModel implements Serializable {
    private static final long serialVersionUID = 4359709211352400087L;

    public String queueName;
    public String uuid;
    public int clientToken;
    public int deviceType;

    @Override
    public String toString() {
        return "MQMapModel{" +
                "queueName='" + queueName + '\'' +
                ", uuid='" + uuid + '\'' +
                ", clientToken=" + clientToken +
                ", deviceType=" + deviceType +
                '}';
    }
}
