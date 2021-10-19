package com.example.imlib.entity;

import netty.entity.NimMsg;

public class RecCacheEntity {
    public int tryCount;
    public long unpackTime;
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
}
