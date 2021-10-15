package netty.entity;

import io.netty.util.internal.StringUtil;
import utils.UUIDUtil;

import java.util.HashMap;
import java.util.Map;

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
    public Map<Integer, Object> msg;
    //需要另一端返回的数据
    public Map<Integer, Object> receipt;

    public void swapUuid() {
        String tmp = from;
        from = to;
        to = tmp;
    }

    public String getGroupId() {
        return (String) msg.get(MsgType.KEY_UNIFY_GROUP_ID);
    }

    public synchronized void recPut(Integer key, Object value) {
        if (receipt == null)
            receipt = new HashMap<>();
        receipt.put(key, value);
    }

    public synchronized void recRemove(Integer key) {
        if (receipt == null)
            return;
        receipt.remove(key);
    }

    public synchronized void msgPut(Integer key, Object value) {
        if (msg == null)
            msg = new HashMap<>();
        msg.put(key, value);
    }

    public synchronized <T> T msgGet(Integer key) {
        if (msg == null || !msg.containsKey(key))
            return null;
        return (T) msg.get(key);
    }

    public synchronized void msgRemove(Integer key) {
        if (msg == null)
            return;
        msg.remove(key);
    }

    public synchronized String tokenService() {
        String token = "";
        if (receipt != null && receipt.containsKey(MsgType.KEY_UNIFY_SERVICE_MSG_TOKEN))
            token = (String) receipt.get(MsgType.KEY_UNIFY_SERVICE_MSG_TOKEN);
        if (StringUtil.isNullOrEmpty(token)) {
            token = UUIDUtil.getUid();
            recPut(MsgType.KEY_UNIFY_SERVICE_MSG_TOKEN, token);
        }

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