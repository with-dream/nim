package com.example.server.utils.analyse;

import entity.Entity;

import java.util.Map;

public class AnalyseEntity extends Entity {
    public long msgId;
    //服务器第一次收到消息的时间
    public long startTime;
    //服务器最后一次收到消息的时间
    public long lastTime;
    //数据的大小
    public int len;
    //消息的级别
    public int level;
    //消息的类型
    public int msgType;
    //clientToken-item键值对
    public Map<Long, Item> items;
    //mq-客户端个数
    public Map<String, Integer> mqCount;

    public static class Item extends Entity {
        public String mqName;
        public long sendTime;
        public long recTime;
        public int status;
        public int retry;
    }
}
