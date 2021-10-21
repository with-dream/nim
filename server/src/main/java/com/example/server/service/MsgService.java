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
        switch (msg.msgType) {
            case MsgType.TYPE_HEART_PING:
                msg.swapUuid();
                msg.msgType = MsgType.TYPE_HEART_PONG;
                channel.writeAndFlush(msg);
                break;
            case MsgType.TYPE_CMD:
                int cmd = NullUtil.isInt(msg.msgMap().get(MsgType.KEY_CMD));
                if (cmd > 1000 && cmd < 3000) {
                    reqService.requestMsg(msg);
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
                that.sendHolder.sendMsg(msg);
                buildRecMsg(channel, msg);
                that.cacheHolder.cacheMsg(msg);
                break;
            case MsgType.TYPE_RECEIPT:
                //要确保调用顺序
                //先将超时队列中的消息移除
                that.sendHolder.checkRecMsg(msg);
                //再移除多余的消息
                clearServerKey(msg);
//                L.e("TYPE_RECEIPT msg==>" + msg);
                //然后转发
                that.sendHolder.sendMsg(msg);
                //最后返回确认消息
                buildRecMsg(channel, msg);
                that.cacheHolder.cacheMsg(msg);
                break;
            default:

                break;
        }

    }

    private void clearServerKey(NimMsg msg) {
        msg.recMap().remove(MsgType.KEY_UNIFY_SERVICE_MSG_TOKEN);
    }

    /**
     * 严格模式下 服务器给客户端发送的消息收到确认
     */
    private void buildRecMsg(Channel channel, NimMsg msg) {
        if (msg.level == MsgLevel.LEVEL_STRICT) {
            NimMsg recMsg = new NimMsg();
            recMsg.from = Constant.SERVER_UID;
            recMsg.to = msg.from;
            recMsg.msgType = MsgType.TYPE_RECEIPT;
            recMsg.recMap().put(MsgType.KEY_RECEIPT_TYPE, msg.msgType);
            recMsg.recMap().put(MsgType.KEY_RECEIPT_MSG_ID, msg.msgId);
            recMsg.recMap().put(MsgType.KEY_RECEIPT_STATE, MsgType.STATE_RECEIPT_SERVER_SUCCESS);
            channel.writeAndFlush(recMsg);
        }
    }
}
