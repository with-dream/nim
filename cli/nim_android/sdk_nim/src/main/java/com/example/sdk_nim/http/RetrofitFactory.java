package com.example.sdk_nim.http;

import android.widget.Toast;

import com.example.sdk_nim.BaseApp;
import com.example.sdk_nim.Config;
import com.example.sdk_nim.utils.NetUtils;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Interceptor;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitFactory {
    private final HttpClient httpClient = new HttpClient();
    private Converter.Factory gsonConverterFactory = GsonConverterFactory.create();
    private CallAdapter.Factory rxJavaCallAdapterFactory = RxJava3CallAdapterFactory.create();
    private Retrofit.Builder retrofit_builder;

    /**
     * 创建ApiService
     *
     * @param baseUrl     网络基地址
     * @param service     具体的ApiService类
     * @param interceptor 需要添加的拦截器
     * @return T 创建完成的ApiService实例
     */
    public <T> T createHttp(String baseUrl, Class<T> service, Interceptor... interceptor) {
        return createHttp(baseUrl, service, false, interceptor);
    }

    public <T> T createHttp(String baseUrl, Class<T> service, boolean ignoreVerifier, Interceptor... interceptor) {
        return createBuilder(baseUrl, ignoreVerifier, interceptor).build().create(service);
    }

    /**
     * 创建Retrofit.Builder
     *
     * @param baseUrl     网络基地址
     * @param interceptor 需要添加的拦截器
     */
    public Retrofit.Builder createBuilder(String baseUrl, Interceptor... interceptor) {
        return createBuilder(baseUrl, false, interceptor);
    }

    public Retrofit.Builder createBuilder(String baseUrl, boolean ignoreVerifier, Interceptor... interceptor) {
        if (retrofit_builder == null) {
            retrofit_builder = new Retrofit.Builder()
                    .client(httpClient.build(ignoreVerifier, interceptor)) //添加自己的client
                    .baseUrl(baseUrl)
                    .addCallAdapterFactory(rxJavaCallAdapterFactory)
                    .addConverterFactory(gsonConverterFactory);
        }
        return retrofit_builder;
    }

    /**
     * 用于操作实际的网络请求
     *
     * @param observable ApiService接口的具体请求方法
     * @param callBack   请求接口回调 T为具体的返回模型类
     */
    public <T> void subscribe(Observable<T> observable, final CallBack<T> callBack) {
        if (!NetUtils.isNetworkConnected()) {
            Toast.makeText(BaseApp.baseApp, "请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<T>() {
                               @Override
                               public void onSubscribe(Disposable d) {

                               }

                               @Override
                               public void onNext(T t) {
                                   callBack.onSuccess(t);
                               }

                               @Override
                               public void onError(Throwable e) {
                                   callBack.onFailure(e.getMessage());
                                   if (Config.DEBUG)
                                       e.printStackTrace();
                               }

                               @Override
                               public void onComplete() {
                               }
                           }
                );
    }
}
