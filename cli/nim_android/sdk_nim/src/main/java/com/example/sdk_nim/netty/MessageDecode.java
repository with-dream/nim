package com.example.sdk_nim.netty;

import com.alibaba.fastjson.JSON;
import com.example.sdk_nim.netty.entity.MsgType;
import com.example.sdk_nim.netty.entity.NimMsg;
import com.example.sdk_nim.utils.AESUtil;
import com.example.sdk_nim.utils.L;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MessageDecode extends ByteToMessageDecoder {
    public static final int HEAD_LENGTH = 4 + 1;

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

        boolean aes = byteBuf.readBoolean();

        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] body = new byte[dataLength];
        byteBuf.readBytes(body);

        if (aes)
            body = AESUtil.decryptAES(body, AESUtil.strKey2SecretKey(IMContext.instance().encrypt.aesKey));

        String tmp = new String(body);

        NimMsg msg = JSON.parseObject(tmp, NimMsg.class);
        if (msg.msgType != MsgType.TYPE_HEART_PING && msg.msgType != MsgType.TYPE_HEART_PONG)
            L.p("c decode==>" + tmp);
        msg.sync();
        list.add(msg);
    }
}
//Can not set ConcurrentHashMap field  to java.util.HashMap