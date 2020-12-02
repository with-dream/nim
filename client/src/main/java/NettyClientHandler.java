import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
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
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("客户端Active .....");
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BaseMsgModel baseMsgModel) throws Exception {
        System.err.println("客户端channelRead0 ....." + baseMsgModel.toString());

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
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}