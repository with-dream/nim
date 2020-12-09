package com.example.imlib.user;

import java.sql.Date;
import java.util.Arrays;

public class UserResultModel {
    public String[] imUrl;
    public String name;
    public long uuid;
    public Date registTime;
    public int code;

    @Override
    public String toString() {
        return "UserResultModel{" +
                "imUrl=" + (imUrl == null ? "" : Arrays.toString(imUrl)) +
                ", name='" + name + '\'' +
                ", uuid=" + uuid +
                ", registTime=" + registTime +
                ", code=" + code +
                '}';
    }
}
