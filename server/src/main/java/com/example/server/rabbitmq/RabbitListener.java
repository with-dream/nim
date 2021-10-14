package com.example.server.rabbitmq;

import com.example.server.netty.SessionHolder;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import netty.MQWrapper;
import netty.MessageDecode;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import netty.model.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;
import utils.L;

import java.util.Set;

/**
 * mq消息转发 收到的消息都是本服务器的消息 可以直接推送到客户端
 */
@Component(RabbitListener.LISTENER_TAG)
public class RabbitListener implements ChannelAwareMessageListener {
    public static final String LISTENER_TAG = "RabbitListener";

    private Gson gson = new Gson();

    @Override
    public void onMessage(Message message, Channel channel) {
        byte[] bytes = message.getBody();
        NimMsg msg = gson.fromJson(new String(bytes), NimMsg.class);
        process(msg);
    }

    private void process(NimMsg msg) {
        switch (msg.msgType) {
            case MsgType.TYPE_CMD:
            case MsgType.TYPE_MSG:
            case MsgType.TYPE_PACK:
            case MsgType.TYPE_RECEIPT:
            case MsgType.TYPE_ROOT:
                SessionHolder.sendMsg(msg, false);
                break;
            case MsgType.TYPE_CMD_GROUP:
            case MsgType.TYPE_GROUP:
                sendMsgGroup(msg);
                break;
        }
    }

    //TODO 同一条消息发送同服务器的多人 可以优化
    private void sendMsgGroup(NimMsg msg) {
        Object uuidSetObj = msg.msg.get(MsgType.KEY_UNIFY_SERVICE_GROUP_UUID_LIST);
        if (uuidSetObj == null) throw new RuntimeException("转发消息的目标uuid为空");
        Set<String> uuidSet = (Set<String>) uuidSetObj;
        for (String uuid : uuidSet) {
            MsgModel msgModel = MsgModel.createPer(msg.from, uuid, msg.fromToken);
            SessionHolder.sendMsg(msg, false);
        }
    }
}
