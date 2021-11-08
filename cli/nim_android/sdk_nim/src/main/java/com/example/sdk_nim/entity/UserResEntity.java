package com.example.sdk_nim.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class UserResEntity implements Serializable {
    public String name;
    public String uuid;
    public Date registerTime;
    public List<String> serviceList;
    public String token;
    public String rsaPublicKey;
    public String aesPublicKey;

    @Override
    public String toString() {
        return "UserResEntity{" +
                ", name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", registerTime=" + registerTime +
                ", serviceList=" + serviceList +
                ", token=" + token +
                ", rsaPublicKey=" + rsaPublicKey +
                ", aesPublicKey=" + aesPublicKey +
                '}';
    }
}
