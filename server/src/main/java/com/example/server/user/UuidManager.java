package com.example.server.user;

import java.util.concurrent.atomic.AtomicLong;

public class UuidManager {
    private AtomicLong uuid;

    private static class UuidManagerHoler {
        private static UuidManager instance = new UuidManager();
    }

    private UuidManager() {
    }

    public static UuidManager getInstance() {
        return UuidManagerHoler.instance;
    }

    public void setUuid(long uuid) {
        this.uuid = new AtomicLong(uuid);
    }

    public long getUuid() {
        return this.uuid.incrementAndGet();
    }
}
