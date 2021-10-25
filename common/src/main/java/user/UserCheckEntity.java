package user;

import entity.Entity;

import java.util.List;

public class UserCheckEntity extends Entity {
    public String name;
    public String uuid;
    public long registerTime;
    public List<String> serviceList;
    public String token;

    @Override
    public String toString() {
        return "UserResultEntity{" +
                ", name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", registerTime=" + registerTime +
                ", serviceList=" + serviceList +
                ", token=" + token +
                '}';
    }
}
