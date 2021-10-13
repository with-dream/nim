package com.example.imlib.netty;

import com.example.imlib.Context;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import netty.model.BaseMsgModel;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class IMContext {
    public String uuid;
    private List<String> ipList;
    public Channel channel;
    public Context context;
    private IMConnCallback callback;
    private IMMsgCallback msgCallback;
    public int clientToken;
    public boolean logout = false;
    private NettyClient nettyClient = new NettyClient();
    public ConcurrentHashMap<Long, BaseMsgModel> receiptMsg = new ConcurrentHashMap<>();

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }

    public void sendMsg(BaseMsgModel msg) {
        this.sendMsg(msg, false);
    }

    public void sendMsg(BaseMsgModel msg, boolean receipt) {
        msg.delayTime = System.currentTimeMillis();
        if (receipt)
            receiptMsg.put(msg.msgId, msg);

        ChannelFuture future = channel.writeAndFlush(msg);
        future.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {

            }
        });
    }

    public void init(Context context) {
        this.context = context;
    }

    public IMConnCallback getCallback() {
        return callback;
    }

    public void setCallback(IMConnCallback callback) {
        this.callback = callback;
    }

    public IMMsgCallback getMsgCallback() {
        return msgCallback;
    }

    public void setMsgCallback(IMMsgCallback callback) {
        this.msgCallback = callback;
    }

    public void clear() {
        channel = null;
    }

    private static class IMManagerHoler {
        private static IMContext instance = new IMContext();
    }

    private IMContext() {
    }

    public static IMContext getInstance() {
        return IMManagerHoler.instance;
    }

    public static void main(String[] args) {

    }

    public int connect() {
        return nettyClient.connect(this.ipList);
    }

}
