package com.example.server.entity;

import entity.Entity;

public class AESEntity extends Entity {
    public String privateRSAServerKey;
    public String publicRSAServerKey;
    public String publicRSAClientKey;
    public String aesKey;
    public long createTime;

    @Override
    public String toString() {
        return "AESEntity{" +
                "privateRSAServerKey='" + privateRSAServerKey + '\'' +
                ", publicRSAServerKey='" + publicRSAServerKey + '\'' +
                ", publicRSAClientKey='" + publicRSAClientKey + '\'' +
                ", aesKey='" + aesKey + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
