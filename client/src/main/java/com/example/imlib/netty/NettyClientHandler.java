package com.example.imlib.netty;

import com.example.imlib.utils.L;
import com.google.gson.Gson;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import netty.model.*;
import utils.Constant;

import java.util.concurrent.TimeUnit;

/**
 * @author Gjing
 * <p>
 * 客户端处理器
 **/
public class NettyClientHandler extends SimpleChannelInboundHandler<BaseMsgModel> {
    private static final int TRY_COUNT_MAX = 5;
    private Gson gson = new Gson();

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        IMContext.getInstance().clear();
        ctx.channel().close();

        if (!IMContext.getInstance().logout)
            IMContext.getInstance().connect();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        ctx.executor().scheduleAtFixedRate(() -> {
            if (!IMContext.getInstance().receiptMsg.isEmpty()) {
                IMContext.getInstance().receiptMsg.forEach((k, v) -> {
                    IMContext.getInstance().channel.writeAndFlush(v);
                    v.tryCount++;

                    if (v.tryCount >= TRY_COUNT_MAX) {
                        L.e("重发失败==>" + v.toString());
                        IMContext.getInstance().receiptMsg.remove(k);
                    }
                });
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BaseMsgModel baseMsgModel) throws Exception {
        if (!(baseMsgModel instanceof MsgCmd) || ((MsgCmd) baseMsgModel).cmd != MsgCmd.HEART)
            L.p(("客户端channelRead0 ==>" + baseMsgModel.toString()));
        switch (baseMsgModel.type) {
            case MsgType.MSG_CMD:
                MsgCmd cmdMsg = (MsgCmd) baseMsgModel;
                switch (cmdMsg.cmd) {
                    case MsgCmd.HEART:

                        break;
                }
                break;
            case MsgType.MSG_CMD_REQ:
                RequestMsgModel reqMsg = (RequestMsgModel) baseMsgModel;
                switch (reqMsg.cmd) {
                    case RequestMsgModel.REQUEST_FRIEND:
                        //请求好友 默认同意
                        reqMsg.cmd = RequestMsgModel.REQUEST_FRIEND_AGREE;

                        String tmp = reqMsg.from;
                        reqMsg.from = reqMsg.to;
                        reqMsg.to = tmp;
                        channelHandlerContext.channel().writeAndFlush(reqMsg);
                        break;
                    case RequestMsgModel.FRIEND_DEL:
                    case RequestMsgModel.FRIEND_DEL_EACH:
                        //请求好友 默认同意
                        L.p("删除好友 来自:" + reqMsg.from);
                        break;
                    case RequestMsgModel.GROUP_ADD_AGREE:
                        L.p("加入群成功 来自:" + reqMsg.groupId);
                        break;
                    case RequestMsgModel.GROUP_EXIT:
                        L.p("退出群成功 来自:" + reqMsg.from);
                        break;
                    case RequestMsgModel.GROUP_ADD:
                        if (IMContext.getInstance().getMsgCallback() != null)
                            IMContext.getInstance().getMsgCallback().receive(reqMsg);
                        break;
                }
                receiptMsg(baseMsgModel);
                break;
            case MsgType.MSG_PERSON:
                MsgCmd person = (MsgCmd) baseMsgModel;
                if (IMContext.getInstance().getMsgCallback() != null)
                    IMContext.getInstance().getMsgCallback().receive(person);
                receiptMsg(baseMsgModel);
                break;
            case MsgType.MSG_GROUP:
                if (IMContext.getInstance().getMsgCallback() != null)
                    IMContext.getInstance().getMsgCallback().receive(baseMsgModel);
                receiptMsg(baseMsgModel);
                break;
            case MsgType.MSG_PACK:
                PackMsgModel packModel = (PackMsgModel) baseMsgModel;

                if (IMContext.getInstance().getMsgCallback() != null)
                    IMContext.getInstance().getMsgCallback().receive(packModel);
                receiptMsg(baseMsgModel);
                break;
            case MsgType.MSG_RECEIPT:
                //将回执消息存储
                ReceiptMsgModel receiptMsgModel = (ReceiptMsgModel) baseMsgModel;
                switch (receiptMsgModel.cmd) {
                    case MsgCmd.SERVER_RECEIVED:
                        IMContext.getInstance().receiptMsg.remove(receiptMsgModel.sendMsgId);
                        L.p("消息发送成功==>" + receiptMsgModel.seq);
                        break;
                    case MsgCmd.RECEIVED:
                        L.p("消息已送达==>" + receiptMsgModel.toString());
                        break;
                }
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
                    MsgModel heart = MsgModel.create(IMContext.getInstance().uuid, Constant.SERVER_UID, IMContext.getInstance().clientToken);
                    heart.cmd = MsgCmd.HEART;
                    IMContext.getInstance().sendMsg(heart);
                    break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public void receiptMsg(BaseMsgModel baseMsgModel, int type) {
        //消息回执
        ReceiptMsgModel receiptModel = ReceiptMsgModel.create(baseMsgModel.to, baseMsgModel.from, baseMsgModel.msgId, IMContext.getInstance().clientToken);
        receiptModel.cmd = type;
        receiptModel.sendMsgType = baseMsgModel.type;
        receiptModel.toToken = baseMsgModel.fromToken;
        ChannelFuture msgFuture = IMContext.getInstance().channel.writeAndFlush(receiptModel);
        msgFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (!future.isSuccess()) {

                }
            }
        });
    }

    public void receiptMsg(BaseMsgModel baseMsgModel) {
        this.receiptMsg(baseMsgModel, MsgCmd.RECEIVED);
    }
}