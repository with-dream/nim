package com.example.server;

import com.example.server.netty.NettyServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import utils.L;

import java.net.InetSocketAddress;

@MapperScan("com.example.server.mapper")
@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        int port = 8090;
        for (String str : args) {
            String[] ss = str.split("=");
            switch (ss[0]) {
                case "p":
                    port = Integer.valueOf(ss[1]);
                    break;
            }
            L.p("str3==>" + str);
        }
        ApplicationRunnerImpl.MQ_NAME = port + "";

        SpringApplication.run(ServerApplication.class, args);
        NettyServer nettyServer = new NettyServer();
        nettyServer.start(new InetSocketAddress("127.0.0.1", port));
    }
}
