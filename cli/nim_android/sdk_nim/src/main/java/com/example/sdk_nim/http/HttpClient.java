package com.example.sdk_nim.http;

import com.example.sdk_nim.Config;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * creat by mz  2019.9.24
 * <p>
 * okhttp封装
 */
public class HttpClient extends OkHttpClient {
    // 请求超时时间
    private static final int DEFAULT_TIMEOUT = 30;

    private Builder builder;

    /**
     * 创建Builder 用于灵活的配置
     */
    public Builder createBuilder() {
        if (builder == null)
            builder = new Builder();

        /**
         * 设置超时和重新连接
         */
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        builder.writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(true);
        if (Config.DEBUG)
            builder.addNetworkInterceptor(new MyHttpLogInterceptor());

        return builder;
    }

    /**
     * 创建OkHttpClient
     *
     * @param interceptors 拦截器
     */
    public OkHttpClient build(boolean ignoreVerifier, Interceptor... interceptors) {
        if (builder == null)
            createBuilder();

        if (interceptors != null)
            for (Interceptor it : interceptors)
                builder.addInterceptor(it);

        if (ignoreVerifier)
            builder.hostnameVerifier((hostname, session) -> true);

        return builder.build();
    }
}
