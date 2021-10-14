package utils;

import java.util.Arrays;
import java.util.List;

public class Constant {
    public static int SUCC = 0;
    public static int FAILED = 1;
    public static final String SERVER_UID = "000000";
    public static final int SERVER_TOKEN = 0;

    public static final int TCP = 0;
    public static final int UDP = 1;
    public static final int WS = 2;

    public static final int NORMAL = 0;
    public static final int STRICT = 1;
    /**
     * 只有udp为严格模式
     */
    public static final int STRICT_UDP = 2;
    /**
     * 消息的严格模式
     */
    public static final int STRICT_MODE = NORMAL;

    public static final int APP = 0;
    public static final int PC = 1;
    public static final int WEB = 2;
    public static final int[] clientType = {APP, PC, WEB};

    public static final int MAC = 10;
    public static final int WINDOW = 11;
    public static final int LINUX = 12;
    public static final int IPHONE = 13;
    public static final int ANDROID = 14;

    public static int mapDevice(int deviceType) {
        switch (deviceType) {
            case MAC:
            case WINDOW:
            case LINUX:
                return PC;
            case IPHONE:
            case ANDROID:
                return APP;
            case WEB:
                return WEB;
            default:
                throw new RuntimeException("mapDevice==>未知类型  deviceInfo:" + deviceType);
        }
    }

    public static final List<String> SERVER_LIST = Arrays.asList("127.0.0.1:8090");
}
