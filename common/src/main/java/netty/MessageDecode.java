package netty;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import netty.entity.NimMsg;

import java.util.List;

public class MessageDecode extends ByteToMessageDecoder {
    public static final int HEAD_LENGTH = 4 + 4;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        if (byteBuf.readableBytes() < HEAD_LENGTH) {
            return;
        }
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        if (dataLength < 0) {
            channelHandlerContext.close();
        }

        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] body = new byte[dataLength];
        byteBuf.readBytes(body);
        String tmp = new String(body);
        NimMsg msg = JSON.parseObject(tmp, NimMsg.class);
        msg.sync();
        list.add(msg);
    }
}
//Can not set ConcurrentHashMap field  to java.util.HashMap