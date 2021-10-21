package user;

import entity.Entity;

import java.util.Date;
import java.util.List;

public class UserEntity extends Entity {
    public String name;
    public String pwd;
    public String uuid;
    public Date registerTime;
    public List<String> serviceList;
}
