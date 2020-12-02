package com.example.server;

import com.example.server.netty.NettyServer;
import com.example.server.user.UuidManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;

@MapperScan("com.example.server.mapper")
@SpringBootApplication
public class ServerApplication {
//    @Autowired
//    SysService sysService;

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
        //TODO 初始化时 初始uuid
        //应该放在一个独立的服务中
        UuidManager.getInstance().setUuid(100);

        NettyServer nettyServer = new NettyServer();
        nettyServer.start(new InetSocketAddress("127.0.0.1", 8090));
    }

}
