package utils;

public class StrUtil {
    public static String[] getStr(String a, String b) {
        String[] str = new String[2];
        if (a.compareTo(b) < 0) {
            str[0] = a;
            str[1] = b;
        } else {
            str[0] = b;
            str[1] = a;
        }
        return str;
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
}
