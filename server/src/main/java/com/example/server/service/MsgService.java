package com.example.server.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import org.springframework.stereotype.Component;
import utils.Constant;

@Component
public class MsgService {

    /**
     * 1 先缓存消息
     * 2 根据消息等级 判断是否需要回执客户端
     * 3 如果是命令消息 则在服务器进行处理
     * 4 发送消息
     *      4.1 如果是单机 则直接转发
     *      4.2 如果是集群 则从redis获取需要推送的uuid-mq映射 推送到相应的服务器 再推送到客户端
     * 5 如果有异常情况 需要回执 如发送失败。。。
     * 6 如果离线用户 则缓存消息
     * 7 如果是严格模式 则服务端创建消息缓存队列 只有收到回执消息才移除
     *
     * tip:
     *  命令消息单独存储 因为是小概率事件
     *
     * */
    public void process(NimMsg msg, Channel channel) {
        switch (msg.msgType) {
            case MsgType.TYPE_HEART_PING:
                msg.swapUuid();
                msg.msgType = MsgType.TYPE_HEART_PONG;
                channel.writeAndFlush(msg);
                break;
            case MsgType.TYPE_CMD:
            case MsgType.TYPE_MSG:
            case MsgType.TYPE_RECEIPT:

                break;
            case MsgType.TYPE_CMD_GROUP:
            case MsgType.TYPE_GROUP:

                break;
        }
    }
}
