package netty.model;

import entity.Entity;
import io.netty.channel.Channel;

import java.lang.ref.WeakReference;

public class ReceiptCacheModel extends Entity {
    public BaseMsgModel msgModel;
    public WeakReference<Channel> channel;

    public String token() {
        return msgModel.msgId + "" + msgModel.toToken;
    }

    @Override
    public String toString() {
        return "ReceiptCacheModel{" +
                "msgModel=" + msgModel +
                ", channel=" + channel +
                '}';
    }
}
