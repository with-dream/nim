package utils;

import java.util.HashSet;
import java.util.Set;

public class NullUtil {
    public static boolean isTrue(Object obj) {
        if (obj == null) return false;
        return (boolean) obj;
    }

    public static <T> Set<T> isSet(Object obj) {
        if (obj == null) return new HashSet<>();
        return (Set<T>) obj;
    }

    public static int isInt(Object obj) {
        if (obj == null) return Integer.MIN_VALUE;
        return (int) obj;
    }

    public static long isLong(Object obj) {
        if (obj == null) return Long.MIN_VALUE;
        return (long) obj;
    }
}
