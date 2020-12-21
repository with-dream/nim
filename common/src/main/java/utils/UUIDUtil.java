package utils;

import java.util.UUID;

public class UUIDUtil {
    public static String getUid() {
        return UUID.randomUUID().toString();
    }

    public static int getUUIDHash() {
        int uid = UUID.randomUUID().toString().hashCode();
        return Math.abs(uid);
    }

    public static int getClientToken() {
        return getUUIDHash();
    }

    public static long getMsgId() {
        int id = getUUIDHash();
        long time = System.currentTimeMillis() & 0xFFFFFFFF;
        long msgId = (time << 32) | (long) id;
        return Math.abs(msgId);
    }
}
