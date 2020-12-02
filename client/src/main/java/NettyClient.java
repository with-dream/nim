import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import netty.model.CmdMsgModel;
import netty.model.MsgModel;
import netty.model.MsgType;

/**
 * @author Gjing
 **/
public class NettyClient {

    public void start(String ip, int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                //该参数的作用就是禁止使用Nagle算法，使用于小数据即时传输
                .option(ChannelOption.TCP_NODELAY, true)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());

        try {
            final ChannelFuture future = bootstrap.connect(ip, port).sync();
            System.err.println("客户端成功....");
            //发送消息
            new Thread(new Runnable() {
                public void run() {

                    try {
                        Thread.currentThread().sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    CmdMsgModel cmdMsgModel = new CmdMsgModel();
                    cmdMsgModel.cmd = CmdMsgModel.LOGIN;
                    cmdMsgModel.from = 111;
                    cmdMsgModel.to = 222;
                    cmdMsgModel.timestamp = System.currentTimeMillis();
                    ChannelFuture cmdFuture = future.channel().writeAndFlush(cmdMsgModel);
                    cmdFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                        public void operationComplete(Future<? super Void> future) throws Exception {
                            System.err.println("client cmd send succ");
                        }
                    });
                    while (true) {
                        try {
                            Thread.currentThread().sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        MsgModel msgModel = new MsgModel();
                        msgModel.type = MsgType.MSG_PERSON;
                        msgModel.from = 111;
                        msgModel.to = 222;
                        msgModel.info = "客户端111 发送";
                        msgModel.timestamp = System.currentTimeMillis();
                        ChannelFuture msgFuture = future.channel().writeAndFlush(msgModel);
                        msgFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                            public void operationComplete(Future<? super Void> future) throws Exception {
//                                System.err.println("client msg send succ");
                            }
                        });
                    }
                }
            }).start();

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}