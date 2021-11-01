package com.example.sdk_nim.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class UserCheckEntity implements Serializable {
    public String name;
    public String uuid;
    public Date registerTime;
    public List<String> serviceList;
    public String token;
    public String rsaPublicKey;

    @Override
    public String toString() {
        return "UserResultEntity{" +
                ", name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", registerTime=" + registerTime +
                ", serviceList=" + serviceList +
                ", token=" + token +
                ", rsaPublicKey=" + rsaPublicKey +
                '}';
    }
}
