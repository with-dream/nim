package com.example.sdk_nim;

import android.app.Application;
import android.content.res.Resources;
import android.os.StrictMode;

/**
 * Created by smz on 2021/6/8.
 */

public class BaseApp extends Application {
    public static BaseApp baseApp;

    @Override
    public void onCreate() {
        super.onCreate();
        baseApp = this;
    }

    protected void debug() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
    }

    public static Resources getRes() {
        return baseApp.getResources();
    }

}
