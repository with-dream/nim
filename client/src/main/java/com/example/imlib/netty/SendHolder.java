package com.example.imlib.netty;

import com.example.imlib.netty.entity.RecCacheEntity;
import com.example.imlib.utils.L;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import netty.entity.*;
import utils.NullUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SendHolder {
    public Map<Long, RecCacheEntity> recMsg = new ConcurrentHashMap<>();

    public void sendHeart(Channel channel, NimMsg msg) {
        SendUtil.sendMsg(channel, 0, msg);
    }

    public void send(Channel channel, NimMsg msg) {
        if (channel != null) {
            ChannelFuture future = SendUtil.sendMsg(channel, msg.fromToken, msg);
            future.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> f) throws Exception {
                    if (f.isSuccess()) {

                    } else {
                        L.p("==>send TYPE_RECEIPT  " + f.cause());

                        if (msg.level == MsgLevel.LEVEL_LOW) {
                            cacheRecMsg(msg);
                        } else if (msg.level == MsgLevel.LEVEL_NORMAL) {
                            if (msg.msgType == MsgType.TYPE_RECEIPT) {
                                cacheRecMsg(msg);
                            }
                        }
                    }

                    if (NimMsg.isRecMsg(msg)) {
                        cacheRecMsg(msg);
                    }

                    future.removeListener(this);
                }
            });
        }
    }

    private void cacheRecMsg(NimMsg msg) {
        RecCacheEntity rce = new RecCacheEntity(msg);
        recMsg.put(msg.msgId, rce);
    }

    public boolean isRec(NimMsg msg) {
        return msg.msgType != MsgType.TYPE_HEART_PING && msg.msgType != MsgType.TYPE_HEART_PONG;
    }

    public void recMsg(NimMsg msg) {
        if (msg.msgType != MsgType.TYPE_RECEIPT) return;
        long msgId = NullUtil.isLong(msg.recMap().get(MsgType.KEY_RECEIPT_MSG_ID));
        recMsg.remove(msgId);
    }
}
//