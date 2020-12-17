package netty;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import netty.model.*;

import java.util.List;

public class MessageDecode extends ByteToMessageDecoder {
    public static final int HEAD_LENGTH = 4 + 4;
    private Gson gson = new Gson();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < HEAD_LENGTH) {
            return;
        }
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        if (dataLength < 0) {
            channelHandlerContext.close();
        }

        int msgType = byteBuf.readInt();
        if (dataLength < 0) {
            channelHandlerContext.close();
        }

        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] body = new byte[dataLength];
        byteBuf.readBytes(body);

        list.add(getModel(gson, msgType, new String(body)));
    }

    public static final BaseMsgModel getModel(Gson gson, int msgType, String body) {
        Class cls = null;
        switch (msgType) {
            case MsgType.CMD_MSG:
                cls = CmdMsgModel.class;
                break;
            case MsgType.MSG_PERSON:
            case MsgType.MSG_GROUP:
                cls = MsgModel.class;
                break;
            case MsgType.RECEIPT_MSG:
                cls = ReceiptMsgModel.class;
                break;
            case MsgType.REQ_CMD_MSG:
                cls = RequestMsgModel.class;
                break;
        }

        return (BaseMsgModel) gson.fromJson(body, cls);
    }
}
