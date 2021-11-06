package user;

import entity.Entity;

public class BaseEntity<T> extends Entity {
    public static final int SUCC = 0;
    public static final int FAIL = 1;
    public static final int FAIL_SERVER = 12;
    public static final int FAIL_TOKEN = 11;

    public int code;
    public String msg;
    public T data;

    public boolean success() {
        return code == SUCC;
    }

    public static BaseEntity failServer() {
        return fail(FAIL_SERVER);
    }

    public static BaseEntity fail() {
        return fail(FAIL);
    }

    public static BaseEntity fail(int code) {
        return fail(code, "fail");
    }

    public static BaseEntity fail(int code, String msg) {
        BaseEntity res = new BaseEntity<>();
        res.code = code;
        res.msg = msg;
        return res;
    }

    public static BaseEntity succ() {
        return succ(null);
    }

    public static <T> BaseEntity<T> succ(T t) {
        BaseEntity<T> res = new BaseEntity<>();
        res.code = SUCC;
        res.msg = "success";
        res.data = t;
        return res;
    }
}
