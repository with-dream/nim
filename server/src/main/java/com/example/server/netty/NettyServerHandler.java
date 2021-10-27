package com.example.server.netty;

import com.alibaba.fastjson.JSON;
import com.example.server.redis.RConst;
import com.example.server.service.MsgService;
import com.example.server.utils.Const;
import com.example.server.utils.analyse.AnalyseEntity;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import utils.L;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author Gjing
 * <p>
 * netty服务端处理器
 **/
@Component
public class NettyServerHandler extends SimpleChannelInboundHandler<NimMsg> {
    private static final int TRY_COUNT_MAX = 3;

    private static NettyServerHandler that;

    @PostConstruct
    public void init() {
        that = this;
    }

    @Resource
    public MsgService msgService;

    @Resource
    public SendHolder sendHolder;

    @Resource
    public RedissonClient redisson;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        L.e("channelInactive==>" + ctx.channel().attr(SendHolder.UUID_CHANNEL_MAP).get());

        that.sendHolder.logout(ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.executor().scheduleAtFixedRate(() -> {
            if (!SendHolder.receiptMap.isEmpty()) {
                SendHolder.receiptMap.forEach((k, v) -> {
                    if (v.tryCount >= TRY_COUNT_MAX) {
                        L.e("重发失败 channel为空==>" + v.toString());
                        sendHolder.removeRecMsg(k);
                    }
                });
            }
        }, 5, 10, TimeUnit.MINUTES);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NimMsg msg) {
        if (msg.msgType == MsgType.TYPE_MSG
                || msg.msgType == MsgType.TYPE_RECEIPT
                || msg.msgType == MsgType.TYPE_GROUP)
            setSeq(msg);

        if (msg.msgType != MsgType.TYPE_HEART_PING)
            if (Const.ANALYSE_DEBUG) {
                RMap<Long, AnalyseEntity> map = that.redisson.getMap(RConst.TEST_ANALYSE);
                //添加回执信息
                if (msg.msgType == MsgType.TYPE_RECEIPT) {
                    RLock lock = that.redisson.getLock(msg.fromToken + "");
                    try {
                        long msgId = (long) msg.recMap().get(MsgType.KEY_RECEIPT_MSG_ID);
                        AnalyseEntity tmp = map.get(msgId);
                        AnalyseEntity.Item item = tmp.items.get(msg.fromToken);
                        item.recTime = System.currentTimeMillis();
                        item.recMsgId = msg.msgId;
                        item.status = 10;
                        map.put(msgId, tmp);
                    } finally {
                        lock.unlock();
                    }
                } else {
                    AnalyseEntity ae = new AnalyseEntity();
                    ae.msgId = msg.msgId;
                    ae.uuid = msg.from;
                    if (msg.msgType == MsgType.TYPE_GROUP || msg.msgType == MsgType.TYPE_CMD_GROUP)
                        ae.groupId = msg.getGroupId();
                    ae.level = msg.level;
                    ae.msgType = msg.msgType;
                    ae.startTime = System.currentTimeMillis();
                    ae.len = JSON.toJSONString(msg).getBytes().length;
                    map.put(msg.msgId, ae);
                }
            }

        that.msgService.process(msg, ctx.channel());
    }

    private void setSeq(NimMsg msg) {
        String tl = "atom:" + MsgCacheHolder.getTimeLine(msg);
        RAtomicLong atomicLong = that.redisson.getAtomicLong(tl);
        //
        if (atomicLong.get() > Long.MAX_VALUE - 100)
            atomicLong.set(1);
        msg.seq = atomicLong.getAndIncrement();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;

            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                that.sendHolder.logout(ctx.channel());
            }
        }
    }

    /**
     * 发生异常触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        L.e("exceptionCaught==>" + ctx.channel().attr(SendHolder.UUID_CHANNEL_MAP).get());
        that.sendHolder.logout(ctx.channel());
        cause.printStackTrace();
    }
}
