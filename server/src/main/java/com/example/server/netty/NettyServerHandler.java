package com.example.server.netty;

import com.example.server.redis.TagList;
import com.example.server.service.MsgService;
import com.example.server.service.RequestService;
import com.example.server.utils.Const;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import netty.entity.NimMsg;
import netty.entity.RequestMsgModel;
import org.springframework.stereotype.Component;
import utils.Constant;
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
    private static final int TRY_COUNT_MAX = 5;

    public static final int WEEK_SECOND = 7 * 24 * 60 * 60;
    public static final int MONTH_SECOND = 30 * 24 * 60 * 60;

    private static NettyServerHandler that;

    @PostConstruct
    public void init() {
        that = this;
    }

    @Resource
    public MsgService msgService;

    @Resource
    public SessionServerHolder holder;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        that.holder.logout(ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        ctx.executor().scheduleAtFixedRate(() -> {
            if (!SessionHolder.receiptMsg.isEmpty()) {
                SessionHolder.receiptMsg.forEach((k, v) -> {
                    if (System.currentTimeMillis() - v.msgModel.sendTime < (v.msgModel.tryCount + 1) * 500)
                        if (v.channel != null && v.channel.get() != null) {
                            v.channel.get().writeAndFlush(v);
                            v.msgModel.tryCount++;

                            if (v.msgModel.tryCount >= TRY_COUNT_MAX) {
                                L.e("重发失败==>" + v.toString());
                                SessionHolder.receiptMsg.remove(k);
                            }
                        } else {
                            L.e("重发失败 channel为空==>" + v.toString());
                            SessionHolder.receiptMsg.remove(k);
                        }

                });
            }
        }, 5, 8, TimeUnit.SECONDS);
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
                that.holder.logout(ctx.channel());
            }
        }
    }

    /**
     * 发生异常触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //TODO 将崩溃放入日志
        that.holder.logout(ctx.channel());
        cause.printStackTrace();
    }
}
