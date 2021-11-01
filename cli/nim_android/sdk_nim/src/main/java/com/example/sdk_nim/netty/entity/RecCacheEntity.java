package com.example.sdk_nim.netty.entity;

public class RecCacheEntity {
    public int tryCount;
    public long unpackTime;
    public int status;  //1 收到服务端的回执
    public NimMsg msg;

    public RecCacheEntity() {
    }

    public RecCacheEntity(NimMsg msg) {
        this.unpackTime = System.currentTimeMillis();
        this.msg = msg;
        updateTime();
    }

    public void updateTime() {
        switch (tryCount) {
            case 0:
                unpackTime = System.currentTimeMillis() + 500;
                tryCount++;
                break;
            case 1:
                unpackTime = System.currentTimeMillis() + 2000;
                tryCount++;
                break;
            case 2:
                unpackTime = System.currentTimeMillis() + 5000;
                tryCount++;
                break;
        }
    }

    public boolean isTimeout() {
        return tryCount >= 3;
    }

    @Override
    public String toString() {
        return "RecCacheEntity{" +
                "tryCount=" + tryCount +
                ", unpackTime=" + unpackTime +
                ", msg=" + msg +
                '}';
    }
}
