package netty.entity;

import utils.UUIDUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 存储消息
 * 为了使消息更加灵活 以及减少体积 除必要内容 全部放在Map中
 * 由于会有并发操作 所以Map会使用Collections.synchronizedMap加锁
 * <p>
 * 反序列化时 只能获取到HashMap 需要调用sync()方法重新初始化锁
 */
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
        return (String) msgMap().get(MsgType.KEY_UNIFY_GROUP_ID);
    }

    public Map<Integer, Object> msgMap() {
        if (msg == null)
            synchronized (this) {
                if (msg == null)
                    msg = Collections.synchronizedMap(new HashMap<>());
            }
        return msg;
    }

    public Map<Integer, Object> recMap() {
        if (receipt == null)
            synchronized (this) {
                if (receipt == null)
                    receipt = Collections.synchronizedMap(new HashMap<>());
            }
        return receipt;
    }

    /**
     * 为msg临时添加一个token
     * 同一条消息的msgId是固定的 如果发送给同一个用户的不同客户端 无法精准知道哪个客户端没有收到 所以添加临时token
     */
    public String newTokenService(boolean save) {
        String token = UUIDUtil.getUid();
        if (save)
            recMap().put(MsgType.KEY_UNIFY_SERVICE_MSG_TOKEN, token);
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

    /**
     * 用于反序列化 增加Map同步
     */
    public void sync() {
        if (msg == null)
            msg = new HashMap<>();
        msg = Collections.synchronizedMap(msg);

        if (receipt == null)
            receipt = new HashMap<>();
        receipt = Collections.synchronizedMap(receipt);
    }

    /**
     * 判断消息是否需要客户端回执
     */
    public static boolean isRecMsg(NimMsg msg) {
        return msg.level == MsgLevel.LEVEL_STRICT
                || (msg.level == MsgLevel.LEVEL_NORMAL && msg.msgType != MsgType.TYPE_RECEIPT);
    }

    @Override
    public String toString() {
        return "NimMsg{" +
                "msgType=" + msgType +
                ", level=" + level +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", msgId=" + msgId +
                ", msg=" + msg +
                ", receipt=" + receipt +
                ", deviceType=" + deviceType +
                ", fromToken=" + fromToken +
                '}';
    }
}
