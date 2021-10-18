package com.example.server.netty;

import com.example.server.service.MsgService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import netty.entity.NimMsg;
import org.springframework.stereotype.Component;
import utils.L;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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
                        SessionHolder.receiptMsg.remove(k);
                    }
                });
            }
        }, 5, 10, TimeUnit.MINUTES);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NimMsg msg) {
        msgService.process(msg, ctx.channel());
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
        //TODO 将崩溃放入日志
        that.sendHolder.logout(ctx.channel());
        cause.printStackTrace();
    }
}
