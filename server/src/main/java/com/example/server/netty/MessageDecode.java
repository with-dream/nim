package com.example.server.netty;

import com.alibaba.fastjson.JSON;
import com.example.server.entity.AESEntity;
import com.example.server.redis.RConst;
import com.example.server.utils.Const;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import netty.entity.MsgType;
import netty.entity.NimMsg;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import utils.AESUtil;
import utils.L;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Component
public class MessageDecode extends ByteToMessageDecoder {
    public static final int HEAD_LENGTH = 4 + 4;

    private static MessageDecode that;

    public MessageDecode() {
    }

    @PostConstruct
    public void init() {
        that = this;
    }

    @Resource
    public RedissonClient redisson;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        if (byteBuf.readableBytes() < HEAD_LENGTH)
            return;
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        if (dataLength < 0)
            channelHandlerContext.close();

        long clientToken = -1;
        boolean aes = byteBuf.readBoolean();
        if (aes) {
            if (byteBuf.readableBytes() < 8) {
                byteBuf.resetReaderIndex();
                return;
            }
            clientToken = byteBuf.readLong();
        }
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] body = new byte[dataLength];
        byteBuf.readBytes(body);

        if (aes) {
            if (clientToken < 0) throw new RuntimeException("clientToken 解析错误");
            RMap<Long, AESEntity> aesMap = that.redisson.getMap(RConst.AES_MAP);
            AESEntity aesEntity = aesMap.get(clientToken);
            if (aesEntity == null) {
                L.e("MessageDecode s clientToken==>" + clientToken);
            }

            body = AESUtil.decryptAES(body, AESUtil.strKey2SecretKey(aesEntity.aesKey));
        }

        String tmp = new String(body);
        NimMsg msg = JSON.parseObject(tmp, NimMsg.class);
        if (msg.msgType != MsgType.TYPE_HEART_PING && msg.msgType != MsgType.TYPE_HEART_PONG)
            L.p("s decode==>" + tmp);
        msg.sync();
        list.add(msg);
    }
}
//Can not set ConcurrentHashMap field  to java.util.HashMap