package netty;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import netty.entity.NimMsg;
import utils.L;

public class MessageEncode extends MessageToByteEncoder<NimMsg> {
    private Gson gson = new Gson();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NimMsg o, ByteBuf byteBuf) throws Exception {
        String ss = null;
        try {
            ss = gson.toJson(o);
        } catch (Exception e) {
            System.err.println(String.format("%s  %s", "encode e==>" + e.toString(), o));
        }
        assert ss != null;
        byte[] data = ss.getBytes();
        L.p("encode==>" + ss);
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}
