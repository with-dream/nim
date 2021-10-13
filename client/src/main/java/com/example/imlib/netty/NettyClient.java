package com.example.imlib.netty;

import java.util.concurrent.TimeUnit;

import com.example.imlib.utils.L;
import com.example.imlib.utils.StrUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import netty.model.MsgCmd;
import netty.model.MsgModel;
import utils.Constant;


/**
 * @author Gjing
 **/
public class NettyClient {
    private EventLoopGroup group;
    private int reconnCount;

    public int connect(String[] ipList) {
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
                    IMContext.getInstance().channel = future.channel();
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
                IMContext.getInstance().connect();
            }
        }, time, TimeUnit.SECONDS);
        reconnCount++;

        return Constant.FAILED;
    }

    private void login() {
        MsgModel cmdMsgModel = MsgModel.create(IMContext.getInstance().uuid, Constant.SERVER_UID, 0);
        cmdMsgModel.cmd = MsgCmd.LOGIN;
        cmdMsgModel.timestamp = System.currentTimeMillis();
        cmdMsgModel.fromToken = IMContext.getInstance().clientToken;
        cmdMsgModel.deviceType = Constant.ANDROID;
        L.e("login==>");

        ChannelFuture cmdFuture = IMContext.getInstance().channel.writeAndFlush(cmdMsgModel);
        cmdFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                System.err.println("client cmd send succ");
                if (IMContext.getInstance().getCallback() != null) {
                    IMContext.getInstance().getCallback().lonin(future.isSuccess()
                            ? Constant.SUCC : Constant.FAILED);
                }
            }
        });
    }
}