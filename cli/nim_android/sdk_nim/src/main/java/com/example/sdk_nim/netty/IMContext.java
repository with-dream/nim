package com.example.sdk_nim.netty;

import com.example.sdk_nim.entity.EncryptEntity;
import com.example.sdk_nim.netty.entity.NimMsg;

import io.netty.channel.Channel;

import java.lang.ref.WeakReference;
import java.util.List;

public class IMContext {
    public String uuid;
    private List<String> ipList;
    public WeakReference<Channel> channel;
    private IMConnCallback callback;
    private IMMsgCallback msgCallback;
    public EncryptEntity encrypt = new EncryptEntity();
    public long clientToken;
    public boolean logout = false;

    private NettyClient nettyClient = new NettyClient();
    public SendHolder sendHolder = new SendHolder();

    private IMContext() {
    }

    private static class IMManagerHolder {
        private static IMContext instance = new IMContext();
    }

    public static IMContext instance() {
        return IMManagerHolder.instance;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }

    public void sendMsg(NimMsg msg) {
        sendHolder.send(channel.get(), msg);
    }

    public void receiveMsg(NimMsg msg) {
        if (msgCallback != null)
            msgCallback.receive(msg);
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

    public boolean checkChannel() {
        if (channel == null || channel.get() == null || !channel.get().isActive())
            return false;
        return true;
    }

    public void clear() {
        channel = null;
    }

    public static void main(String[] args) {

    }

    public int connect() {
        return nettyClient.connect(this.ipList);
    }
}
