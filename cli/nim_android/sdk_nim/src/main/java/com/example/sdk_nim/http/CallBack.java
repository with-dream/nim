package com.example.sdk_nim.http;

public interface CallBack<T> {
    void onSuccess(T entity);
    void onFailure(String msg);
}
