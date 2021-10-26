package com.example.server;

import com.example.server.netty.NettyServer;
import com.example.server.netty.SendHolder;
import com.example.server.redis.RConst;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import utils.Constant;
import utils.L;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

@MapperScan("com.example.server.mapper")
@SpringBootApplication
public class ServerApplication {
    @Resource
    RedissonClient redisson;

    public static void main(String[] args) {
        int port = Constant.PORT;
//        for (String str : args) {
//            String[] ss = str.split("=");
//            switch (ss[0]) {
//                case "p":
//                    port = Integer.valueOf(ss[1]);
//                    break;
//            }
//            L.p("str3==>" + str);
//        }
        ApplicationRunnerImpl.MQ_NAME = port + "";

        SpringApplication.run(ServerApplication.class, args);
        NettyServer nettyServer = new NettyServer();
        nettyServer.start(new InetSocketAddress("127.0.0.1", port));
    }

    @Bean
    public ExitCodeGenerator exitCodeGenerator() {
        return () -> {
            L.p("==>");
            RSet<String> mqList = redisson.getSet(RConst.MQ_SET);
            mqList.remove(ApplicationRunnerImpl.MQ_NAME);
            return 42;
        };
    }
}
