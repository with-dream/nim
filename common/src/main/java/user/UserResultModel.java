package user;

import java.sql.Date;
import java.util.Arrays;

public class UserResultModel {
    public String[] imUrl;
    public String name;
    public String uuid;
    public int clientToken;
    public Date registTime;
    public int code;

    @Override
    public String toString() {
        return "UserResultModel{" +
                "imUrl=" + Arrays.toString(imUrl) +
                ", name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", clientToken=" + clientToken +
                ", registTime=" + registTime +
                ", code=" + code +
                '}';
    }
}
