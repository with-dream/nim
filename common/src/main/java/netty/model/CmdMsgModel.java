package netty.model;

import java.io.Serializable;

public class CmdMsgModel extends BaseMsgModel implements Serializable {
    public static final int LOGIN = 0;
    public static final int LOGOUT = 1;
    public static final int HEART = 3;

    public static final int SEND_SUC = 10;
    public static final int RECEIVED = 11;
    public static final int READED = 12;

    public static final int MAC = 0;
    public static final int WINDOW = 1;
    public static final int LINUX = 2;
    public static final int IPHONE = 3;
    public static final int ANDROID = 4;

    public int cmd;

    //客户端类型 电脑 手机等 用于唯一标识一个平台
    //客户登录时使用sendRabbitLogin方法
    public DeviceType deviceType;

    public static CmdMsgModel create(String from, String to, int clientToken) {
        CmdMsgModel cmdMsgModel = new CmdMsgModel();
        cmdMsgModel.createMsgId();
        cmdMsgModel.type = MsgType.MSG_CMD;
        cmdMsgModel.from = from;
        cmdMsgModel.to = to;
        cmdMsgModel.fromToken = clientToken;
        return cmdMsgModel;
    }

    @Override
    public String toString() {
        return "CmdMsgModel{" +
                "cmd=" + cmd +
                ", deviceType=" + deviceType +
                ", type=" + type +
                ", seq=" + seq +
                ", timeLine=" + timeLine +
                ", msgId=" + msgId +
                ", fromToken=" + fromToken +
                ", toToken=" + toToken +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", timestamp=" + timestamp +
                ", tryCount=" + tryCount +
                '}';
    }
}
