package netty.entity;

import io.netty.util.internal.StringUtil;
import utils.UUIDUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NimMsg implements Cloneable {
    //消息id 保证唯一 规则
    public long msgId;
    //发送者uuid
    public String from;
    //接收者uuid
    public String to;
    //客户端类型 电脑 手机等 用于唯一标识一个平台
    public int deviceType;
    //设备token 用于标记客户端平台
    public int fromToken;
    //消息类型
    public int msgType;
    //消息等级
    public int level;

    //消息
    private ConcurrentHashMap<Integer, Object> msg;
    //需要另一端返回的数据
    private ConcurrentHashMap<Integer, Object> receipt;

    public void swapUuid() {
        String tmp = from;
        from = to;
        to = tmp;
    }

    public String getGroupId() {
        return (String) getMsgMap().get(MsgType.KEY_UNIFY_GROUP_ID);
    }

    public synchronized Map<Integer, Object> getMsgMap() {
        if (msg == null)
            msg = new ConcurrentHashMap<>();
        return msg;
    }

    public synchronized Map<Integer, Object> getRecMap() {
        if (receipt == null)
            receipt = new ConcurrentHashMap<>();
        return receipt;
    }

    /**
     * 为msg临时添加一个token
     * 同一条消息的msgId是固定的 如果发送给同一个用户的不同客户端 无法精准知道哪个客户端没有收到 所以添加临时token
     * */
    public String newTokenService() {
        String token = UUIDUtil.getUid();
        getRecMap().put(MsgType.KEY_UNIFY_SERVICE_MSG_TOKEN, token);
        return token;
    }

    public NimMsg copy() {
        NimMsg res = null;
        try {
            res = (NimMsg) this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return res;
    }
}
