package utils;

public class Constant {
    public static int SUCC = 0;
    public static int FAILED = 1;
    public static final String SERVER_UID = "000000";
    public static final int SERVER_TOKEN = 0;

    public static final int APP = 0;
    public static final int PC = 1;
    public static final int WEB = 2;
    public static final int[] clientType = {APP, PC, WEB};

    public static final int MAC = 10;
    public static final int WINDOW = 11;
    public static final int LINUX = 12;
    public static final int IPHONE = 13;
    public static final int ANDROID = 14;

    public static int mapDevice(int deviceInfo) {
        switch (deviceInfo) {
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
                throw new RuntimeException("mapDevice==>未知类型  deviceInfo:" + deviceInfo);
        }
    }

    public static final String SERVER_LIST[] = {"127.0.0.1:8090"};
}
