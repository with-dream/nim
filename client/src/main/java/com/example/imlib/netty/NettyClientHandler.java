package com.example.imlib.netty;

import com.example.imlib.utils.L;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import netty.model.*;

/**
 * @author Gjing
 * <p>
 * 客户端处理器
 **/
public class NettyClientHandler extends SimpleChannelInboundHandler<BaseMsgModel> {
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        IMContext.getInstance().clear();
        ctx.channel().close();

        IMContext.getInstance().connect();
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BaseMsgModel baseMsgModel) throws Exception {
        if (!(baseMsgModel instanceof CmdMsgModel) || ((CmdMsgModel) baseMsgModel).cmd != CmdMsgModel.HEART)
            L.p(("客户端channelRead0 ==>" + baseMsgModel.toString()));
        switch (baseMsgModel.type) {
            case MsgType.CMD_MSG:
                CmdMsgModel cmdMsg = (CmdMsgModel) baseMsgModel;
                switch (cmdMsg.cmd) {
                    case CmdMsgModel.HEART:

                        break;
                }
                break;
            case MsgType.REQ_CMD_MSG:
                RequestMsgModel reqMsg = (RequestMsgModel) baseMsgModel;
                switch (reqMsg.cmd) {
                    case RequestMsgModel.REQUEST_FRIEND:
                        //请求好友 默认同意
                        reqMsg.cmd = RequestMsgModel.REQUEST_FRIEND_AGREE;

                        long tmp = reqMsg.from;
                        reqMsg.from = reqMsg.to;
                        reqMsg.to = tmp;
                        channelHandlerContext.channel().writeAndFlush(reqMsg);
                        break;
                    case RequestMsgModel.DEL_FRIEND:
                    case RequestMsgModel.DEL_FRIEND_EACH:
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
                break;
            case MsgType.MSG_PERSON:
                MsgModel person = (MsgModel) baseMsgModel;
                if (IMContext.getInstance().getMsgCallback() != null)
                    IMContext.getInstance().getMsgCallback().receive(person);

                //消息回执
                ReceiptMsgModel receiptModel = new ReceiptMsgModel();
                receiptModel.type = MsgType.RECEIPT_MSG;
                receiptModel.cmd = CmdMsgModel.RECEIVED;
                receiptModel.from = baseMsgModel.to;
                receiptModel.to = baseMsgModel.from;
                receiptModel.timestamp = System.currentTimeMillis();
                receiptModel.receipt = baseMsgModel.msgId;
                ChannelFuture msgFuture = channelHandlerContext.channel().writeAndFlush(receiptModel);
                msgFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                    public void operationComplete(Future<? super Void> future) throws Exception {
//                System.err.println("client msg send succ");
                    }
                });
                break;
            case MsgType.MSG_GROUP:
                String groupLine = "msg_group:" + baseMsgModel.to;
                if (IMContext.getInstance().getMsgCallback() != null)
                    IMContext.getInstance().getMsgCallback().receive(baseMsgModel);

                break;
            case MsgType.RECEIPT_MSG:
                //将回执消息存储
                L.p("消息已送达==>" + baseMsgModel.toString());
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
                    CmdMsgModel heart = CmdMsgModel.create(IMContext.getInstance().uuid, 0);
                    heart.cmd = CmdMsgModel.HEART;
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
}