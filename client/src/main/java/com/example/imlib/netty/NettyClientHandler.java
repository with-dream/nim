package com.example.imlib.netty;

import com.example.imlib.netty.entity.RecCacheEntity;
import com.example.imlib.utils.L;
import com.example.imlib.utils.MsgBuild;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import netty.entity.SendUtil;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Gjing
 * <p>
 * 客户端处理器
 **/
public class NettyClientHandler extends SimpleChannelInboundHandler<NimMsg> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        IMContext.instance().clear();
        ctx.channel().close();

        if (!IMContext.instance().logout)
            IMContext.instance().connect();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        ctx.executor().scheduleAtFixedRate(() -> {
            if (!IMContext.instance().sendHolder.recMsg.isEmpty()) {
                Set<Long> set = IMContext.instance().sendHolder.recMsg.keySet();
                Iterator<Long> it = set.iterator();
                while (it.hasNext()) {
                    long key = it.next();
                    RecCacheEntity v = IMContext.instance().sendHolder.recMsg.get(key);
                    if (v.unpackTime - System.currentTimeMillis() <= 0) {
                        if (IMContext.instance().checkChannel()) {
                            SendUtil.sendMsg(IMContext.instance().channel.get(), v.msg.fromToken, v.msg);

                            if (v.isTimeout()) {
                                L.e(new Date() + "  重发失败==>" + v.toString());
                                IMContext.instance().sendHolder.recMsg.remove(key);
                            }
                        }
                        v.updateTime();
                    }
                }
            }
        }, 5, 1, TimeUnit.SECONDS);
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, NimMsg msg) throws Exception {
        switch (msg.msgType) {
            case MsgType.TYPE_HEART_PONG:

                break;
            case MsgType.TYPE_CMD:
            case MsgType.TYPE_CMD_GROUP:
                IMContext.instance().receiveMsg(msg);
                break;
            case MsgType.TYPE_MSG:
            case MsgType.TYPE_GROUP:
            case MsgType.TYPE_ROOT:
                IMContext.instance().receiveMsg(msg);

                NimMsg recMsg = MsgBuild.recMsg(msg.from);
                recMsg.receipt.putAll(msg.receipt);
                recMsg.recMap().put(MsgType.KEY_RECEIPT_TYPE, msg.msgType);
                recMsg.recMap().put(MsgType.KEY_RECEIPT_MSG_ID, msg.msgId);
                recMsg.recMap().put(MsgType.KEY_RECEIPT_STATE, MsgType.STATE_RECEIPT_CLIENT_SUCCESS);
                IMContext.instance().sendMsg(recMsg);
                break;
            case MsgType.TYPE_RECEIPT:
                IMContext.instance().sendHolder.recMsg(msg);
                IMContext.instance().receiveMsg(msg);
                break;
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        super.userEventTriggered(ctx, evt);

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case READER_IDLE:

                    break;
                case WRITER_IDLE:

                    break;
                case ALL_IDLE:
                    IMContext.instance().sendHolder.sendHeart(ctx.channel(), MsgBuild.heart());
                    break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}