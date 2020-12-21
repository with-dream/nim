package netty.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PackMsgModel extends BaseMsgModel implements Serializable {
    private List<BaseMsgModel> msgModels = new ArrayList<>();

    public static PackMsgModel create(String from, String to, int clientToken) {
        PackMsgModel cmdMsgModel = new PackMsgModel();
        cmdMsgModel.createMsgId();
        cmdMsgModel.type = MsgType.MSG_PACK;
        cmdMsgModel.from = from;
        cmdMsgModel.to = to;
        cmdMsgModel.fromToken = clientToken;
        return cmdMsgModel;
    }

    public void addMsg(BaseMsgModel msgModel) {
        this.msgModels.add(msgModel);
    }

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
