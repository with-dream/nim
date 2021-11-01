package com.example.sdk_nim.netty;

import com.example.sdk_nim.netty.entity.MsgCmd;
import com.example.sdk_nim.netty.entity.MsgType;
import com.example.sdk_nim.netty.entity.NimMsg;
import com.example.sdk_nim.netty.entity.SendUtil;
import com.example.sdk_nim.utils.Constant;
import com.example.sdk_nim.utils.L;
import com.example.sdk_nim.utils.MsgBuild;
import com.example.sdk_nim.utils.StrUtil;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;


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

        group.schedule(() -> {
            IMContext.instance().connect();
        }, time, TimeUnit.SECONDS);
        reconnCount++;

        return Constant.FAILED;
    }

    private void login() {
        NimMsg loginMsg = MsgBuild.build(MsgType.TYPE_CMD, Constant.SERVER_UID);
        loginMsg.msgMap().put(MsgType.KEY_CMD, MsgCmd.LOGIN);

        ChannelFuture cmdFuture = SendUtil.sendMsg(IMContext.instance().channel.get(), loginMsg.fromToken, loginMsg);
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