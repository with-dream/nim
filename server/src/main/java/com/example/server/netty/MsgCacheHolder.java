package com.example.server.netty;

import netty.entity.MsgType;
import netty.entity.NimMsg;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import utils.StrUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class MsgCacheHolder {
    private static MsgCacheHolder that;

    @PostConstruct
    public void init() {
        that = this;
    }

    @Resource
    public RedissonClient redisson;

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
        that.redisson.getList(tl).add(msg);
        return true;
    }

    /**
     * 保存离线消息 群消息不能保存为离线消息
     */
    public boolean saveOfflineMsg(NimMsg msg) {
        that.redisson.getList(MsgType.CACHE_OFFLINE_MSG + msg.to).add(msg);
        return true;
    }
}
