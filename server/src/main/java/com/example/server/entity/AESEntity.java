package com.example.server.entity;

public class AESEntity {
    public byte[] privateRSAServerKey;
    public byte[] publicRSAServerKey;
    public byte[] publicRSAClientKey;
    public byte[] aesKey;
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
