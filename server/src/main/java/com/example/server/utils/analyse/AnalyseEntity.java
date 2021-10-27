package com.example.server.utils.analyse;

import entity.Entity;

import java.util.List;
import java.util.Map;

public class AnalyseEntity extends Entity {
    public long msgId;
    public String uuid;
    public String groupId;
    //服务器第一次收到消息的时间
    public long startTime;
    //数据的大小
    public int len;
    //消息的级别
    public int level;
    //群用户列表
    public List<String> memberList;
    //消息的类型
    public int msgType;
    //clientToken-item键值对
    public Map<Long, Item> items;
    //mq对应的uuid
    public Map<String, List<String>> mqList;

    public static class Item extends Entity {
        public String mqName;
        public String uuid;
        public long sendTime;
        public long recTime;
        //回执信息发送给发送端的时间
        public long recSendTime;
        public long recMsgId;
        /**
         * 1 发送失败  2 发送成功  3 发送失败并超时 4 发送时目标用户离线 5 消息开始推送
         * 10 发送端服务器接收到回执 11 回执发送失败 12 回执发送成功 13 发送失败并超时 14 回执用户离线 15 回执消息开始推送
         */
        public int status;
        public int retry;
    }
}
