package com.example.imlib.netty;

import com.example.imlib.entity.RecCacheEntity;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import netty.entity.MsgLevel;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import utils.NullUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SendHolder {
    public Map<Long, RecCacheEntity> recMsg = new ConcurrentHashMap<>();

    public void send(Channel channel, NimMsg msg) {
        if (channel != null) {
            ChannelFuture future = channel.writeAndFlush(msg);
            if (msg.level == MsgLevel.LEVEL_NORMAL || msg.level == MsgLevel.LEVEL_STRICT) {
                RecCacheEntity rce = new RecCacheEntity(msg);
                recMsg.put(msg.msgId, rce);
            }
            future.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> f) throws Exception {
                    if (f.isSuccess()) {

                    } else {
                        if (msg.level == MsgLevel.LEVEL_LOW) {
                            RecCacheEntity rce = new RecCacheEntity(msg);
                            recMsg.put(msg.msgId, rce);
                        }
                    }
                    future.removeListener(this);
                }
            });
        }
    }

    public void recMsg(NimMsg msg) {
        if (msg.msgType != MsgType.TYPE_RECEIPT) return;
        long msgId = NullUtil.isLong(msg.getMsgMap().get(MsgType.KEY_RECEIPT_MSG_ID));
        if (msgId <= 0) {
            throw new RuntimeException("recMsg err==>" + msg);
        }
        recMsg.remove(msgId);
    }
}
