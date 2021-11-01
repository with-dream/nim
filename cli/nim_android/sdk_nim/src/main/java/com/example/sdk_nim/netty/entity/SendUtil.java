package com.example.sdk_nim.netty.entity;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public class SendUtil {
    public static ChannelFuture sendMsg(Channel channel, long clientToken, NimMsg msg) {
        NimMsgWrap nmw = new NimMsgWrap();
        nmw.clientToken = clientToken;
        nmw.msg = msg;
        return channel.writeAndFlush(nmw);
    }
}
