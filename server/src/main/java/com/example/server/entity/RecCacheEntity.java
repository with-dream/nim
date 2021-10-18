package com.example.server.entity;

import com.example.server.netty.SessionModel;
import netty.entity.NimMsg;

import java.lang.ref.WeakReference;
import java.util.Comparator;

public class RecCacheEntity implements Comparator<RecCacheEntity> {
    public String token;
    public int tryCount;
    public long unpackTime;
    public WeakReference<SessionModel> sm;
    public NimMsg msg;

    public RecCacheEntity() {
    }

    public RecCacheEntity(int tryCount, WeakReference<SessionModel> sm, NimMsg msg) {
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
    public int compare(RecCacheEntity o1, RecCacheEntity o2) {
        long res = o1.unpackTime - o2.unpackTime;
        if (res > 0) return 1;
        else if (res < 0) return -1;
        return 0;
    }
}
