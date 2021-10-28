package netty.entity;

import utils.NullUtil;
import utils.UUIDUtil;

import java.io.Serializable;
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
public class NimMsg implements Cloneable, Serializable {
    //消息id 保证唯一 规则
    public long msgId;
    //发送者uuid
    public String from;
    //接收者uuid
    public String to;
    //客户端类型 电脑 手机等 用于唯一标识一个平台
    public int deviceType;
    //设备token 用于标记客户端平台
    public long fromToken;
    //消息类型
    public int msgType;

    //消息计数
    public long seq;

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
        return (String) msgMap().get(MsgType.KEY_UNIFY_M_GROUP_ID);
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
            recMap().put(MsgType.KEY_UNIFY_R_SERVICE_MSG_TOKEN, token);
        return token;
    }

//    public NimMsg copy() {
//        NimMsg res = null;
//        try {
//            res = (NimMsg) this.clone();
//        } catch (CloneNotSupportedException e) {
//            e.printStackTrace();
//        }
//
//        return res;
//    }

    /**
     * map中的对象没有进行深clone 不过目前已经够用了
     */
    public NimMsg copyDeep() {
        NimMsg res = null;
        try {
            res = (NimMsg) this.clone();
            if (msg != null) {
                res.msg = Collections.synchronizedMap(new HashMap<>());
                res.msg.putAll(msg);
            }
            if (receipt != null) {
                res.receipt = Collections.synchronizedMap(new HashMap<>());
                res.receipt.putAll(receipt);
            }
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
     * 回执消息不需要再回执
     */
    public boolean isRecClient() {
        return NullUtil.isTrue(msgMap().get(MsgType.KEY_UNIFY_M_REC_CLIENT))
                && msgType != MsgType.TYPE_RECEIPT;
    }

    /**
     * 是否需要直接回执
     */
    public boolean isRecDirect() {
        return NullUtil.isTrue(msgMap().get(MsgType.KEY_UNIFY_M_REC_DIRECT));
    }

    public boolean isRec() {
        return isRecClient() || isRecDirect();
    }

    @Override
    public String toString() {
        return "NimMsg{" +
                "msgType=" + msgType +
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
