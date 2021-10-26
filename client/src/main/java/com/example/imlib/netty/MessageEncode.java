package com.example.imlib.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import netty.entity.NimMsgWrap;
import utils.AESUtil;
import utils.L;

public class MessageEncode extends MessageToByteEncoder<NimMsgWrap> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NimMsgWrap wrap, ByteBuf byteBuf) throws Exception {
        NimMsg msg = wrap.msg;
        //不加密
        boolean aes = (msg.msgType != MsgType.TYPE_HEART_PING
                && msg.msgType != MsgType.TYPE_HEART_PONG);

        String ss = null;
        try {
            ss = JSON.toJSONString(msg, SerializerFeature.DisableCircularReferenceDetect);
        } catch (Exception e) {
            System.err.println(String.format("%s  %s", "encode e==>" + e.toString(), msg));
        }
        assert ss != null;
        byte[] data = ss.getBytes("utf-8");
        if (aes)
            data = AESUtil.encryptAES(data, AESUtil.strKey2SecretKey(IMContext.instance().encrypt.aesKey));

//        if (msg.msgType != MsgType.TYPE_HEART_PING
//                && msg.msgType != MsgType.TYPE_HEART_PONG)
            L.p("c encode==>" + ss);

        byteBuf.writeInt(data.length);
        byteBuf.writeBoolean(aes);
        if (aes)
            byteBuf.writeLong(msg.fromToken);
        byteBuf.writeBytes(data);
    }
}
