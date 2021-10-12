package com.example.server.rabbitmq;

import com.example.server.entity.GroupMsgModel;
import com.example.server.netty.SessionHolder;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import netty.MQWrapper;
import netty.MessageDecode;
import netty.model.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;
import utils.L;

/**
 * mq消息转发 收到的消息都是本服务器的消息 可以直接推送到客户端
 * */
@Component(RabbitListener.LISTENER_TAG)
public class RabbitListener implements ChannelAwareMessageListener {
    public static final String LISTENER_TAG = "RabbitListener";

    private Gson gson = new Gson();

    @Override
    public void onMessage(Message message, Channel channel) {
        byte[] bytes = message.getBody();
        MQWrapper mqWrapper = gson.fromJson(new String(bytes), MQWrapper.class);
        BaseMsgModel baseMsgModel = MessageDecode.getModel(gson, mqWrapper.type, mqWrapper.json);

        L.p("onMessage==>" + baseMsgModel.toString());

        process(baseMsgModel);
    }

    private void process(BaseMsgModel baseMsgModel) {
        switch (baseMsgModel.type) {
            case MsgType.MSG_PACK:
            case MsgType.MSG_RECEIPT:
            case MsgType.MSG_PERSON:
            case MsgType.MSG_CMD_REQ:
                SessionHolder.sendMsg(baseMsgModel, false);
                break;
            case MsgType.MSG_GROUP:
                sendMsgGroup(baseMsgModel);
                break;
        }
    }

    private void sendMsgGroup(BaseMsgModel msg) {
        GroupMsgModel groupModel = (GroupMsgModel) msg;
        for (String uuid : groupModel.toSet) {
            MsgModel msgModel = MsgModel.createP(msg.from, uuid, msg.fromToken);
            SessionHolder.sendMsg(msgModel, false);
        }
    }
}
