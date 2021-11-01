package com.example.sdk_nim.user;

import java.util.Date;
import java.util.List;

public class UserEntity extends Entity {
    public String name;
    public String pwd;
    public String uuid;
    public Date registerTime;
    public List<String> serviceList;
}
