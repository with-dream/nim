package user;

import netty.model.BaseMsgModel;

public class CommUserInfo {
    public BaseMsgModel msgModel;
    public String queueName;

    @Override
    public String toString() {
        return "CommUserInfo{" +
                "msgModel='" + msgModel + '\'' +
                ", queueName='" + queueName + '\'' +
                '}';
    }
}
