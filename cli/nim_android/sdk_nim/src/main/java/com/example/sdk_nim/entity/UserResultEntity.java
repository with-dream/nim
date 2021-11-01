package com.example.sdk_nim.entity;

import java.sql.Date;
import java.util.Arrays;

public class UserResultEntity {
    public String[] imUrl;
    public String name;
    public long uuid;
    public Date registTime;
    public int code;

    @Override
    public String toString() {
        return "UserResultEntity{" +
                "imUrl=" + (imUrl == null ? "" : Arrays.toString(imUrl)) +
                ", name='" + name + '\'' +
                ", uuid=" + uuid +
                ", registTime=" + registTime +
                ", code=" + code +
                '}';
    }
}
