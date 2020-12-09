package netty;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import netty.model.BaseMsgModel;

public class MessageEncode extends MessageToByteEncoder<BaseMsgModel> {
    private Gson gson = new Gson();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, BaseMsgModel o, ByteBuf byteBuf) throws Exception {
        String ss = null;
        try {
            ss = gson.toJson(o);
        } catch (Exception e) {
            System.err.println("e==>" + e.toString());
        }
        byte[] str = ss.getBytes();
//        System.err.println("str==>" + o.toString());
        byteBuf.writeInt(str.length);
        byteBuf.writeInt(o.type);
        byteBuf.writeBytes(str);
    }
}
