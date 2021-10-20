package com.example.imlib.netty;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.imlib.utils.L;
import com.example.imlib.utils.MsgBuild;
import com.example.imlib.utils.StrUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import netty.entity.MsgCmd;
import netty.entity.MsgLevel;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import utils.Constant;


/**
 * @author Gjing
 **/
public class NettyClient {
    private EventLoopGroup group;
    private int reconnCount;

    public int connect(List<String> ipList) {
        if (group != null) {
            group.shutdownGracefully();
        }

        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                //该参数的作用就是禁止使用Nagle算法，使用于小数据即时传输
                .option(ChannelOption.TCP_NODELAY, true)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());

        ChannelFuture future = null;
        for (String ipport : ipList) {
            if (StrUtil.isEmpty(ipport) || !ipport.contains(":"))
                continue;
            String[] ip = ipport.split(":");
            try {
                future = bootstrap.connect(ip[0], Integer.parseInt(ip[1])).sync();
                if (future.isSuccess()) {
                    reconnCount = 0;
                    IMContext.instance().channel = new WeakReference<>(future.channel());
                    L.p("==>客户端成功....");

                    Thread.sleep(200);

                    login();
                    return Constant.SUCC;
                } else
                    continue;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int time = 5;
        if (reconnCount > 3) {
            time = 20;
        } else if (reconnCount > 10) {
            time = 60;
        }

        group.schedule(new Runnable() {
            @Override
            public void run() {
                IMContext.instance().connect();
            }
        }, time, TimeUnit.SECONDS);
        reconnCount++;

        return Constant.FAILED;
    }

    private void login() {
        NimMsg loginMsg = MsgBuild.build(MsgType.TYPE_CMD, Constant.SERVER_UID, MsgLevel.LEVEL_STRICT);
        loginMsg.msgMap().put(MsgType.KEY_CMD, MsgCmd.LOGIN);

        ChannelFuture cmdFuture = IMContext.instance().channel.get().writeAndFlush(loginMsg);
        cmdFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                System.err.println("client cmd send succ");
                if (IMContext.instance().getCallback() != null) {
                    IMContext.instance().getCallback().login(future.isSuccess()
                            ? Constant.SUCC : Constant.FAILED);
                }
                cmdFuture.removeListener(this);
            }
        });
    }
}