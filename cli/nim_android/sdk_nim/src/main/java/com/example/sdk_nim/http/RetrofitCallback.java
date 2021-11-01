package com.example.sdk_nim.http;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by smz on 2021/8/16.
 */

public abstract class RetrofitCallback implements okhttp3.Callback {
    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        if(response.isSuccessful()) {
            onSuccess(call, response);
        } else {
            onFailure(call, new IOException(response.message()));
        }
    }

    public abstract void onSuccess(Call call, Response response);
    //用于进度的回调
    public abstract void onLoading(int index, long total, long progress) ;
}