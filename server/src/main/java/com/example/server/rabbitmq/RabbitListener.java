package com.example.server.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.example.server.netty.SendHolder;
import com.rabbitmq.client.Channel;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;
import utils.L;

import javax.annotation.Resource;

/**
 * mq消息转发 收到的消息都是本服务器的消息 可以直接推送到客户端
 */
@Component(RabbitListener.LISTENER_TAG)
public class RabbitListener implements ChannelAwareMessageListener {
    public static final String LISTENER_TAG = "RabbitListener";

    @Resource
    SendHolder sendHolder;

    @Override
    public void onMessage(Message message, Channel channel) {
        byte[] bytes = message.getBody();
        NimMsg msg = JSON.parseObject(new String(bytes), NimMsg.class);
        msg.sync();
        process(msg);
    }

    private void process(NimMsg msg) {
        switch (msg.msgType) {
            case MsgType.TYPE_CMD:
            case MsgType.TYPE_MSG:
            case MsgType.TYPE_PACK:
            case MsgType.TYPE_RECEIPT:
            case MsgType.TYPE_ROOT:
            case MsgType.TYPE_CMD_GROUP:
            case MsgType.TYPE_GROUP:
                sendHolder.sendMsgLocal(msg);
                break;
        }
    }
}
