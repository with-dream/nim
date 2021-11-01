package com.example.sdk_nim.entity;

public class BaseEntity<T> {
    public int code;
    public String msg;
    public T data;

    @Override
    public String toString() {
        return "BaseEntity{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    public boolean success() {
        return code == 0;
    }
}
