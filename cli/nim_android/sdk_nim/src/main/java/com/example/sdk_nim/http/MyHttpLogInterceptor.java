package com.example.sdk_nim.http;

import android.text.TextUtils;
import android.util.Log;

import com.example.sdk_nim.Config;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * 打印json xml html等含有字符的日志，过滤掉流
 */
public class MyHttpLogInterceptor implements Interceptor {
    private static final String TAG = "=====";

    public MyHttpLogInterceptor() {
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        //添加到责任链中
        Request request = chain.request();
        logForRequest(request);
        Response response = chain.proceed(request);
        return logForResponse(response);
    }

    /**
     * 打印响应日志
     *
     * @param response
     * @return
     */
    private Response logForResponse(Response response) {
        Log.e(TAG, "********响应日志开始********");
        Response.Builder builder = response.newBuilder();
        Response clone = builder.build();
        if (Config.DEBUG) {
            Log.d(TAG, "url:" + clone.request().url());
            Log.d(TAG, "code:" + clone.code());
            if (!TextUtils.isEmpty(clone.message()))
                Log.e(TAG, "message:" + clone.message());
        }
        ResponseBody body = clone.body();
        if (body != null) {
            MediaType mediaType = body.contentType();
            if (mediaType != null) {
                if (isText(mediaType)) {
                    String resp = null;
                    try {
                        resp = body.string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (Config.DEBUG) {
                        Log.d(TAG, "响应:" + resp);
                        Log.e(TAG, "********响应日志结束********");
                    }
                    body = ResponseBody.create(mediaType, resp);
                    return response.newBuilder().body(body).build();
                } else {
                    if (Config.DEBUG)
                        Log.e(TAG, "响应内容 : " + "发生错误-非文本类型");
                }
            }
        }
        Log.e(TAG, "********响应日志结束********");
        return response;
    }

    private boolean isText(MediaType mediaType) {
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        if (mediaType.subtype() != null) {
            if (mediaType.subtype().equals("json")
                    || mediaType.subtype().equals("xml")
                    || mediaType.subtype().equals("html")
                    || mediaType.subtype().equals("webviewhtml")
                    || mediaType.subtype().equals("x-www-form-urlencoded")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 打印请求日志
     *
     * @param request
     */
    private void logForRequest(Request request) {
        String url = request.url().toString();
        RequestBody requestBody = request.body();
        if (Config.DEBUG) {
            Log.e(TAG, "========请求日志开始=======");
            Log.d(TAG, "请求方式 : " + request.method());
            Log.d(TAG, "url : " + url);
            Log.d(TAG, "headers : " + request.headers());
        }
        if (requestBody != null) {
            MediaType mediaType = requestBody.contentType();
            if (mediaType != null) {
                if (Config.DEBUG) {
                    Log.d(TAG, "请求内容类别 : " + mediaType.toString());
                    if (isText(mediaType)) {
                        Log.d(TAG, "请求内容 : " + bodyToString(request));
                    } else {
                        Log.d(TAG, "请求内容 : " + " 无法识别。");
                    }
                }
            }
        }
        Log.e(TAG, "========请求日志结束=======");
    }

    private String bodyToString(Request request) {
        Request req = request.newBuilder().build();
        String urlSub = null;
        Buffer buffer = new Buffer();
        try {
            req.body().writeTo(buffer);
            urlSub = buffer.readUtf8();
//            urlSub = URLDecoder.decode(message, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
            return "在解析请求内容时候发生了异常-非字符串";
        }
        return urlSub;
    }


}