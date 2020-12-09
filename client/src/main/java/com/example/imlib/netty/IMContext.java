package com.example.imlib.netty;

import com.example.imlib.Context;
import io.netty.channel.Channel;
import netty.model.BaseMsgModel;

public class IMContext {
    public long uuid;
    private String[] ipList;
    public Channel channel;
    public Context context;
    private IMConnCallback callback;
    private IMMsgCallback msgCallback;
    private NettyClient nettyClient = new NettyClient();

    public void setIpList(String[] ipList) {
        this.ipList = ipList;
    }

    public void sendMsg(BaseMsgModel msg) {
        channel.writeAndFlush(msg);
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
