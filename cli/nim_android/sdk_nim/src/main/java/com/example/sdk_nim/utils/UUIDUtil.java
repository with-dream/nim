package com.example.sdk_nim.utils;

import java.util.UUID;

public class UUIDUtil {
    public static String getUid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static int getUUIDHash() {
        int uid = UUID.randomUUID().toString().hashCode();
        return Math.abs(uid);
    }

    public static long getClientToken() {
        return getMsgId();
    }

    public static long getMsgId() {
        int id = getUUIDHash();
        long time = System.currentTimeMillis();
        long msgId = (time << 32) | (long) id;
        return Math.abs(msgId);
    }
}