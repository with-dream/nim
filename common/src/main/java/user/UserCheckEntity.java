package user;

import entity.Entity;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;

public class UserCheckEntity extends Entity {
    public String name;
    public String uuid;
    public long registerTime;
    public List<String> serviceList;

    @Override
    public String toString() {
        return "UserResultEntity{" +
                ", name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", registerTime=" + registerTime +
                ", serviceList=" + serviceList +
                '}';
    }
}
