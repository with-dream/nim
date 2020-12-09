package netty.model;

/**
 * 1、服务器只处理命令
 * 主要是转发的作用
 *
 * 2、登录/退出使用http
 * 登录成功
 *      单点登录
 *      拉取离线数据
 *      下发服务器列表
 *          使用icmp协议 测出最快的服务器 (客户端需要做的)
 *          群使用单独的服务器 (使用不同的服务器 好像也没啥问题)
 *          消息转发 可以使用不同的服务器 因为互相独立
 *          如果在局域网 则直接通信
 *
 * 3、需要支持websocket
 * */

public class BaseMsgModel {
    public int type;

    //消息的序列号
    public long seq = 0;
    //消息队列号 个人聊天 使用两个人的uuid 且合成规则为小+大 群组用群号
    public long timeLine;
    //消息id 保证唯一 规则
    public String msgId;
    //设备token 用于标记平台的客户端
    public int device;
    //发送者uuid
    public long from;
    //接收者uuid
    public long to;
    //时间戳 毫秒 统一为服务器的时间
    public long timestamp;

    public void clear() {

    }

    @Override
    public String toString() {
        return "BaseMsgModel{" +
                "type=" + type +
                ", seq=" + seq +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
