package com.example.nim_android;

import com.example.sdk_nim.BaseApp;

import okhttp3.OkHttpClient;

public class App extends BaseApp {
    public static App app;
    public OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }
}
