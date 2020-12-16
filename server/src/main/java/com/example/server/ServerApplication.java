package com.example.server;

import com.example.server.netty.NettyServer;
import com.example.server.service.SysService;
import com.example.server.user.UuidManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import utils.L;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.Arrays;

@MapperScan("com.example.server.mapper")
@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
        //TODO 初始化时 初始uuid
//        UuidManager.getInstance().init();

        NettyServer nettyServer = new NettyServer();
        nettyServer.start(new InetSocketAddress(Config.LOCAL_IP, 8090));
    }
}
