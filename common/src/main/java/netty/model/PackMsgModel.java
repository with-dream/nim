package netty.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PackMsgModel extends BaseMsgModel implements Serializable {
    public List<String> msgModels = new ArrayList<>();

    public static PackMsgModel create(String from, String to, int clientToken) {
        PackMsgModel cmdMsgModel = new PackMsgModel();
        cmdMsgModel.createMsgId();
        cmdMsgModel.type = MsgType.MSG_PACK;
        cmdMsgModel.from = from;
        cmdMsgModel.to = to;
        cmdMsgModel.fromToken = clientToken;
        return cmdMsgModel;
    }

    public void addMsgId(String msgModel) {
        this.msgModels.add(msgModel);
    }
//
//    public List<BaseMsgModel> parse(Gson gson) {
//        if (msgModels.isEmpty())
//            return null;
//
//        List<BaseMsgModel> list = new ArrayList<>();
//        for (String msg : msgModels) {
//            JsonObject obj = new JsonParser().parse(msg).getAsJsonObject();
//            int type = obj.get("type").getAsInt();
//            list.add(MessageDecode.getModel(gson, type, msg));
//        }
//        baseMsgModels = list;
//        return list;
//    }

    @Override
    public String toString() {
        return "PackMsgModel{" +
                "msgModels=" + msgModels +
                ", type=" + type +
                ", seq=" + seq +
                ", timeLine=" + timeLine +
                ", msgId=" + msgId +
                ", fromToken=" + fromToken +
                ", toToken=" + toToken +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", timestamp=" + timestamp +
                ", status=" + status +
                ", tryCount=" + tryCount +
                '}';
    }
}
