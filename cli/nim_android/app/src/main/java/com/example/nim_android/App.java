package com.example.nim_android;

import com.example.sdk_nim.BaseApp;
import com.example.sdk_nim.entity.UserCheckEntity;

import okhttp3.OkHttpClient;

public class App extends BaseApp {
    public static App app;
    public OkHttpClient okHttpClient = new OkHttpClient();

    public UserCheckEntity entity;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }
}
