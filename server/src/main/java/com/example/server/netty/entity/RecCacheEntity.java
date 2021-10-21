package com.example.server.netty.entity;

import netty.entity.NimMsg;

import java.lang.ref.WeakReference;

public class RecCacheEntity implements Comparable<RecCacheEntity> {
    public String token;
    public int tryCount;
    public long unpackTime;
    public WeakReference<SessionEntity> sm;
    public NimMsg msg;

    public RecCacheEntity() {
    }

    public RecCacheEntity(int tryCount, WeakReference<SessionEntity> sm, NimMsg msg) {
        this.tryCount = tryCount;
        this.sm = sm;
        this.msg = msg;
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
    public int compareTo(RecCacheEntity o) {
        long res = unpackTime - o.unpackTime;
        if (res > 0) return 1;
        else if (res < 0) return -1;
        return 0;
    }
}