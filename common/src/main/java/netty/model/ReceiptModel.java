package netty.model;

import io.netty.channel.Channel;

import java.lang.ref.WeakReference;

public class ReceiptModel {
    public BaseMsgModel msgModel;
    public WeakReference<Channel> channel;

    @Override
    public String toString() {
        return "ReceiptModel{" +
                "msgModel=" + msgModel +
                ", channel=" + channel +
                '}';
    }
}
