//package netty.model;
//
//import java.io.Serializable;
//
//public class MsgCmd extends BaseMsgModel implements Serializable {
//    public static final int LOGIN = 0;
//    public static final int LOGOUT = 1;
//    public static final int HEART = 3;
//
//    public static final int SEND_SUC = 10;
//    public static final int RECEIVED = 11;
//    public static final int READ = 12;
//
//    public static final int SERVER_RECEIVED = 100;
//    public static final int CLIENT_RECEIVED = 101;
//
//    public int cmd;
//
//    public static MsgCmd create(String from, String to, int clientToken) {
//        MsgCmd cmdMsgModel = new MsgCmd();
//        cmdMsgModel.createMsgId();
//        cmdMsgModel.type = MsgType.MSG_CMD;
//        cmdMsgModel.from = from;
//        cmdMsgModel.to = to;
//        cmdMsgModel.fromToken = clientToken;
//        return cmdMsgModel;
//    }
//
//    @Override
//    public String toString() {
//        return "MsgCmd{" +
//                "cmd=" + cmd +
//                ", type=" + type +
//                ", seq=" + seq +
//                ", timeLine=" + timeLine +
//                ", msgId=" + msgId +
//                ", fromToken=" + fromToken +
//                ", toToken=" + toToken +
//                ", from='" + from + '\'' +
//                ", to='" + to + '\'' +
//                ", timestamp=" + timestamp +
//                ", tryCount=" + tryCount +
//                '}';
//    }
//}
