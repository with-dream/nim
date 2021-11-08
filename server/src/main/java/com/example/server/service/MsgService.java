package com.example.server.service;

import com.example.server.netty.MsgBuild;
import com.example.server.netty.MsgCacheHolder;
import com.example.server.netty.SendHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import netty.entity.*;
import org.springframework.stereotype.Component;
import utils.Constant;
import utils.L;
import utils.NullUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class MsgService {

    private static MsgService that;

    @PostConstruct
    public void init() {
        that = this;
    }

    @Resource
    MsgCacheHolder cacheHolder;

//    @Resource
//    RequestService reqService;

    @Resource
    SendHolder sendHolder;

    /**
     * 1 先缓存消息
     * 2 根据消息等级 判断是否需要回执客户端
     * 3 如果是命令消息 则在服务器进行处理
     * 4 发送消息
     * 4.1 如果是单机 则直接转发
     * 4.2 如果是集群 则从redis获取需要推送的uuid-mq映射 推送到相应的服务器 再推送到客户端
     * 5 如果有异常情况 需要回执 如发送失败。。。
     * 6 如果离线用户 则缓存消息
     * 7 如果是严格模式 则服务端创建消息缓存队列 只有收到回执消息才移除
     * <p>
     * tip:
     * 命令消息单独存储 因为是小概率事件
     */
    public void process(NimMsg msg, Channel channel) {
        int ret = MsgType.STATE_RECEIPT_SERVER_SUCCESS;
        switch (msg.msgType) {
            case MsgType.TYPE_HEART_PING:
                msg.swapUuid();
                msg.msgType = MsgType.TYPE_HEART_PONG;

                SendUtil.sendMsg(channel, 0, msg);
                break;
            case MsgType.TYPE_CMD:
                int cmd = NullUtil.isInt(msg.msgMap().get(MsgType.KEY_CMD));
                if (cmd > 1000 && cmd < 3000) {
//                    ret = reqService.requestMsg(msg);
                } else {
                    switch (cmd) {
                        case MsgCmd.LOGIN:
                            that.sendHolder.login(channel, msg);
                            break;
                        case MsgCmd.LOGOUT:
                            that.sendHolder.logout(channel);
                            break;
                    }
                }
                break;
            case MsgType.TYPE_CMD_GROUP:
                that.cacheHolder.cacheMsg(msg);
                break;
            case MsgType.TYPE_MSG:
            case MsgType.TYPE_GROUP:
            case MsgType.TYPE_ROOT:
                L.p("s process msg==>" + msg);
                ret = that.sendHolder.sendMsg(msg);
                that.cacheHolder.cacheMsg(msg);
                break;
            case MsgType.TYPE_RECEIPT:
                //要确保调用顺序
                //先将超时队列中的消息移除
                that.sendHolder.checkRecMsg(msg);
//                L.e("TYPE_RECEIPT msg==>" + msg);
                //然后转发  如果是回执给服务器 则不需要转发
                if (!Constant.SERVER_UID.equals(msg.to))
                    that.sendHolder.sendMsg(msg);
                that.cacheHolder.cacheMsg(msg);
                break;
            default:

                break;
        }

        //如果用户离线 则缓存消息
        if (ret == MsgType.STATE_RECEIPT_OFFLINE
                && (msg.msgType == MsgType.TYPE_MSG
                || msg.msgType == MsgType.TYPE_RECEIPT
                || msg.msgType == MsgType.TYPE_CMD)) {
            that.cacheHolder.saveOfflineMsg(msg);
        }

        if (msg.msgType != MsgType.TYPE_HEART_PING && msg.msgType != MsgType.TYPE_HEART_PONG)
            buildRecMsg(channel, msg, ret);
    }

    /**
     * 严格模式下 服务器给客户端发送的消息收到确认
     * 或者消息出现异常情况 如目标用户不在线等
     */
    private void buildRecMsg(Channel channel, NimMsg msg, int status) {
//        L.p("buildRecMsg isRecDirect==>" + msg.isRecDirect());
        //严格模式 只有消息有回执
        if (msg.isRecDirect()) {
            NimMsg recMsg = MsgBuild.recMsg(Constant.SERVER_UID, msg.from);
            recMsg.msgMap().put(MsgType.KEY_M_RECEIPT_TYPE, msg.msgType);
            recMsg.msgMap().put(MsgType.KEY_M_RECEIPT_MSG_ID, msg.msgId);
            recMsg.msgMap().put(MsgType.KEY_M_RECEIPT_STATE, status);
            SendUtil.sendMsg(channel, msg.fromToken, recMsg).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> f) throws Exception {
                    if (!f.isSuccess() && f.cause() != null)
                        L.e("buildRecMsg " + f.isSuccess() + "  11  " + f.cause());
                }
            });
        }
    }
}
