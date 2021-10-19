package com.example.server.service;

import com.example.server.netty.MsgCacheHolder;
import com.example.server.netty.SendHolder;
import io.netty.channel.Channel;
import netty.entity.MsgCmd;
import netty.entity.MsgLevel;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import org.springframework.stereotype.Component;
import utils.Constant;
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

    @Resource
    RequestService reqService;

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
        boolean cache = true;
        switch (msg.msgType) {
            case MsgType.TYPE_HEART_PING:
                msg.swapUuid();
                msg.msgType = MsgType.TYPE_HEART_PONG;
                channel.writeAndFlush(msg);
                break;
            case MsgType.TYPE_CMD:
                int cmd = NullUtil.isInt(msg.getMsgMap().get(MsgType.KEY_CMD));
                switch (cmd) {
                    case MsgCmd.LOGIN:
                        cache = false;
                        that.sendHolder.login(channel, msg);
                        break;
                    case MsgCmd.LOGOUT:
                        cache = false;
                        that.sendHolder.logout(channel);
                        break;
                }
                break;
            case MsgType.TYPE_CMD_GROUP:

                break;
            case MsgType.TYPE_MSG:
            case MsgType.TYPE_GROUP:
            case MsgType.TYPE_RECEIPT:
            case MsgType.TYPE_ROOT:
                that.sendHolder.sendMsg(msg);
                break;
            default:

                break;
        }

        if (msg.level == MsgLevel.LEVEL_STRICT) {
            NimMsg recMsg = new NimMsg();
            recMsg.from = Constant.SERVER_UID;
            recMsg.to = msg.from;
            recMsg.msgType = MsgType.TYPE_RECEIPT;
            recMsg.getRecMap().put(MsgType.KEY_RECEIPT_TYPE, msg.msgType);
            recMsg.getRecMap().put(MsgType.KEY_RECEIPT_MSG_ID, msg.msgId);
            recMsg.getRecMap().put(MsgType.KEY_RECEIPT_STATE, MsgType.STATE_RECEIPT_SERVER_SUCCESS);
            channel.writeAndFlush(recMsg);
        }

        if (cache) {
            boolean res = that.cacheHolder.cacheMsg(msg);
        }
    }
}
