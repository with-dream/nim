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
                    if (!f.isSuccess()) {
                        if (f.cause() != null)
                            L.e("send cause==>" + f.cause());
                        //如果不需要回执的消息发送失败 则重发
                        if (!msg.isRecClient() && !msg.isRecDirect())
                            cacheRecMsg(msg);
                    }
                    if (msg.isRecClient() || msg.isRecDirect())
                        cacheRecMsg(msg);
                    future.removeListener(this);
                }
            });
        }
    }

    /**
     * 缓存消息
     * 1 需要服务端回执的消息
     * 2 需要客户端回执的消息
     * 3 发送失败的消息
     */
    private void cacheRecMsg(NimMsg msg) {
        RecCacheEntity rce = new RecCacheEntity(msg);
        recMsg.put(msg.msgId, rce);
    }

    /**
     * 收到消息回执 将缓存的消息移除
     */
    public void recMsg(NimMsg msg) {
        if (msg.msgType != MsgType.TYPE_RECEIPT) return;
        long msgId = NullUtil.isLong(msg.msgMap().get(MsgType.KEY_M_RECEIPT_MSG_ID));
        RecCacheEntity rce = recMsg.get(msgId);
        if (rce == null) L.e("recMsg rce为null==>" + msg);

        if (!rce.msg.isRec())
            recMsg.remove(msgId);
        else {
            int status = NullUtil.isInt(msg.msgMap().get(MsgType.KEY_M_RECEIPT_STATE));
            if (rce.msg.isRecClient()) {
                if (status == MsgType.STATE_RECEIPT_SERVER_SUCCESS)
                    rce.status = 1;
                else if (status == MsgType.STATE_RECEIPT_CLIENT_SUCCESS)
                    recMsg.remove(msgId);
                else
                    L.e("recMsg==>移除消息异常");
            } else if (rce.msg.isRecDirect()) {
                if (status == MsgType.STATE_RECEIPT_SERVER_SUCCESS)
                    recMsg.remove(msgId);
            }
        }
    }
}
