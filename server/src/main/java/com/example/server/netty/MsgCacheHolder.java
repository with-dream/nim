package com.example.server.netty;

import netty.entity.MsgType;
import netty.entity.NimMsg;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import utils.StrUtil;

import javax.annotation.Resource;

public class MsgCacheHolder {

    @Resource
    public RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存消息
     */
    public boolean cacheMsg(NimMsg msg) {
        String timeLineTag = "";
        switch (msg.msgType) {
            case MsgType.TYPE_CMD:
                timeLineTag = MsgType.CACHE_CMD;
                break;
            case MsgType.TYPE_CMD_GROUP:
                timeLineTag = MsgType.CACHE_CMD_GROUP;
                break;
            case MsgType.TYPE_MSG:
                timeLineTag = MsgType.CACHE_MSG;
                break;
            case MsgType.TYPE_GROUP:
                timeLineTag = MsgType.CACHE_GROUP;
                break;
            case MsgType.TYPE_RECEIPT:
                timeLineTag = MsgType.CACHE_RECEIPT;
                break;
        }

        if (StringUtils.isEmpty(timeLineTag)) {
            //消息id异常
            return false;
        }
        String tl = StrUtil.getTimeLine(msg.from, msg.to, timeLineTag);
        redisTemplate.opsForList().rightPush(tl, msg);
        return true;
    }

    /**
     * 保存离线消息 群消息不能保存为离线消息
     */
    public boolean saveOfflineMsg(NimMsg msg) {
        redisTemplate.opsForList().rightPush(MsgType.CACHE_OFFLINE_MSG + msg.to, msg);
        return true;
    }
}
