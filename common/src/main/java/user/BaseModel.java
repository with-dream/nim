package user;

import entity.Entity;

public class BaseModel<T> extends Entity {
    public static final int SUCC = 0;
    public static final int FAIL = 1;

    public int code;
    public String msg;
    public T data;

    public static BaseModel fail() {
        return fail(FAIL);
    }

    public static BaseModel fail(int code) {
        return fail(code, "fail");
    }

    public static BaseModel fail(int code, String msg) {
        BaseModel res = new BaseModel<>();
        res.code = code;
        res.msg = msg;
        return res;
    }

    public static BaseModel succ() {
        return succ(null);
    }

    public static <T> BaseModel<T> succ(T t) {
        BaseModel<T> res = new BaseModel<>();
        res.code = SUCC;
        res.msg = "success";
        res.data = t;
        return res;
    }
}
