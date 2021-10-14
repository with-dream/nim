package com.example.server.service;

import io.netty.channel.Channel;
import netty.entity.NimMsg;
import org.springframework.stereotype.Component;
import utils.Constant;

@Component
public class MsgService {
    public void process(NimMsg msg, Channel channel, int type) {
        switch (Constant.STRICT_MODE) {
            case Constant.NORMAL:

                break;
            case Constant.STRICT:
                receipt(msg, channel);
                break;
            case Constant.STRICT_UDP:
                if(type == Constant.UDP) {
                    receipt(msg, channel);
                }
                break;
        }
    }

    private void receipt(NimMsg msg, Channel channel) {
        msg.msgType
    }
}
