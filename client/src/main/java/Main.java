import com.google.gson.Gson;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import user.LoginModel;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final Gson gson = new Gson();

        Request request = new Request.Builder()
                .url(String.format("http://%s:8080/user/login?name=abc&pwd=111", Conf.LOCAL_IP))
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("onFailure==>" + e.toString());
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                LoginModel loginModel = gson.fromJson(res, LoginModel.class);
                String url = loginModel.imUrl[0];
                String[] ip = url.split(":");
                startNetty(ip[0], Integer.parseInt(ip[1]));
            }
        });
    }

    public static void startNetty(String ip, int port) {
        NettyClient nettyClient = new NettyClient();
        nettyClient.start(ip, port);
    }
}
