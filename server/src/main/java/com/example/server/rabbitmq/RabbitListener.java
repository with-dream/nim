package com.example.server.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.example.server.netty.SendHolder;
import com.rabbitmq.client.Channel;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;
import utils.L;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * mq消息转发 收到的消息都是本服务器的消息 可以直接推送到客户端
 */
@Component(RabbitListener.LISTENER_TAG)
public class RabbitListener implements ChannelAwareMessageListener {
    public static final String LISTENER_TAG = "RabbitListener";

    @Resource
    SendHolder sendHolder;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        byte[] bytes = message.getBody();
        String body = null;
        body = new String(bytes, StandardCharsets.UTF_8);
        if (StringUtils.isEmpty(body)) {
            L.e("RabbitListener onMessage body为空");
            return;
        }
        L.e("RabbitListener onMessage body==>" + body);

        NimMsg msg = JSON.parseObject(body, NimMsg.class);
        msg.sync();

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        L.e("RabbitListener onMessage msgId==>" + msg.msgId);
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
