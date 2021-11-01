package com.example.sdk_nim.utils;

public class StrUtil {
    public static UuidCompare uuidCompare(String a, String b) {
        UuidCompare compare = new UuidCompare();
        if (a.compareTo(b) < 0) {
            compare.low = a;
            compare.high = b;
            compare.invert = false;
        } else {
            compare.low = b;
            compare.high = a;
            compare.invert = true;
        }
        return compare;
    }

    public static String getTimeLine(String a, String b, String tag) {
        StringBuilder sb = new StringBuilder();
        sb.append(tag).append(":");
        if (a.compareTo(b) < 0) {
            sb.append(a).append(":").append(b);
        } else {
            sb.append(b).append(":").append(a);
        }

        return sb.toString();
    }

    public static class UuidCompare {
        public String low;
        public String high;
        public boolean invert;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String byteToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String strHex = Integer.toHexString(bytes[i]);
            if (strHex.length() > 3) {
                sb.append(strHex.substring(6));
            } else {
                if (strHex.length() < 2) {
                    sb.append("0" + strHex);
                } else {
                    sb.append(strHex);
                }
            }
        }
        return sb.toString();
    }
}
