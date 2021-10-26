package com.example.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author Gjing
 * <p>
 * netty服务初始化器
 **/
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast("ping", new IdleStateHandler(60, 60, 60, TimeUnit.SECONDS));
        //添加编解码
//        socketChannel.pipeline().addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
//        socketChannel.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
        socketChannel.pipeline().addLast(new MessageDecode());
        socketChannel.pipeline().addLast(new MessageEncode());
        socketChannel.pipeline().addLast(new NettyServerHandler());
    }
}