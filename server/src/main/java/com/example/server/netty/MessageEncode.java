package com.example.server.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.example.server.entity.AESEntity;
import com.example.server.redis.RConst;
import com.example.server.utils.Const;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import netty.entity.NimMsgWrap;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import utils.AESUtil;
import utils.L;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class MessageEncode extends MessageToByteEncoder<NimMsgWrap> {
    private static MessageEncode that;

    public MessageEncode() {
    }

    @PostConstruct
    public void init() {
        that = this;
    }

    @Resource
    public RedissonClient redisson;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NimMsgWrap o, ByteBuf byteBuf) throws Exception {
        NimMsg msg = o.msg;
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
        if (aes) {
            RMap<Long, AESEntity> aesMap = that.redisson.getMap(RConst.AES_MAP);
            AESEntity aesEntity = aesMap.get(o.clientToken);
            if (aesEntity == null)
                throw new RuntimeException("加密错误 msg:" + msg);

            data = AESUtil.encryptAES(data, AESUtil.strKey2SecretKey(new String(aesEntity.aesKey)));
        }
        if (msg.msgType != MsgType.TYPE_HEART_PING && msg.msgType != MsgType.TYPE_HEART_PONG)
            L.p("s encode==>" + ss);

        byteBuf.writeInt(data.length);
        byteBuf.writeBoolean(aes);
        byteBuf.writeBytes(data);
    }
}
