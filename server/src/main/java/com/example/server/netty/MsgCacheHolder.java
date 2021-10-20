package com.example.server.netty;

import netty.entity.MsgType;
import netty.entity.NimMsg;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import utils.L;
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

    public static String getTimeLine(NimMsg msg) {
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
            case MsgType.TYPE_ROOT:
                timeLineTag = MsgType.CACHE_ROOT;
                break;
        }

        String tl = StrUtil.getTimeLine(msg.from, msg.to, timeLineTag);
        if (msg.msgType == MsgType.TYPE_ROOT)
            tl = timeLineTag;
        return tl;
    }

    /**
     * 缓存消息
     */
    public boolean cacheMsg(NimMsg msg) {
        L.e("==>cacheMsg");
//        RList<NimMsg> list = that.redisson.getList(getTimeLine(msg));
//        return list.add(msg);
        return true;
    }

    /**
     * 保存离线消息 群消息不能保存为离线消息
     */
    public boolean saveOfflineMsg(NimMsg msg) {
        return that.redisson.getList(MsgType.CACHE_OFFLINE_MSG + msg.to).add(msg);
    }
}
