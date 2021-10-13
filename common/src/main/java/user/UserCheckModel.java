package user;

import entity.Entity;

import java.sql.Date;
import java.util.Arrays;

public class UserCheckModel extends Entity {
    public String[] imUrl;
    public String name;
    public String uuid;
    public int clientToken;
    public Date registerTime;
    public int code;

    @Override
    public String toString() {
        return "UserResultModel{" +
                "imUrl=" + Arrays.toString(imUrl) +
                ", name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", clientToken=" + clientToken +
                ", registTime=" + registerTime +
                ", code=" + code +
                '}';
    }
}
