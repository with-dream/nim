package user;

import java.sql.Date;
import java.util.Arrays;

public class UserResultModel extends BaseModel {
    public String[] imUrl;
    public String name;
    public long uuid;
    public Date registTime;

    @Override
    public String toString() {
        return "UserResultModel{" +
                "imUrl=" + (imUrl == null ? "" : Arrays.toString(imUrl)) +
                ", name='" + name + '\'' +
                ", uuid=" + uuid +
                ", registTime=" + registTime +
                ", code=" + code +
                '}';
    }
}
