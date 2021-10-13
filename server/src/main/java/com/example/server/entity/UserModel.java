package com.example.server.entity;

import entity.Entity;

import java.util.Date;

public class UserModel extends Entity {
    public String name;
    public String pwd;
    public String uuid;
    public Date registerTime;
}
