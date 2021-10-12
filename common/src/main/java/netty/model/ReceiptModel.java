package netty.model;

import entity.Entity;
import io.netty.channel.Channel;

import java.lang.ref.WeakReference;

public class ReceiptModel extends Entity {
    public BaseMsgModel msgModel;
    public WeakReference<Channel> channel;

    public String token() {
        return msgModel.msgId + "" + msgModel.toToken;
    }

    @Override
    public String toString() {
        return "ReceiptModel{" +
                "msgModel=" + msgModel +
                ", channel=" + channel +
                '}';
    }
}
