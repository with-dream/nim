package com.example.imlib.netty;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import netty.MessageDecode;
import netty.MessageEncode;


/**
 * @author Gjing
 * 客户端初始化器
 **/
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast("ping", new IdleStateHandler(25, 25, 25, TimeUnit.SECONDS));
//        socketChannel.pipeline().addLast("decoder", new StringDecoder());
//        socketChannel.pipeline().addLast("encoder", new StringEncoder());
        socketChannel.pipeline().addLast(new MessageDecode());
        socketChannel.pipeline().addLast(new MessageEncode());
        socketChannel.pipeline().addLast(new NettyClientHandler());
    }
}