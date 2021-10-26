package com.example.server.redis;

public class RConst {
    /**
     * 存储客户端clientToken-aesKey
     */
    public static final String AES_MAP = "AES_MAP";
    /**
     * uuid-mq名字 用于消息转发
     */
    public static final String UUID_MQ_MAP = "mq_map";
    /**
     * 存储所有的mq名称 用于群发消息/服务器退出
     */
    public static final String MQ_SET = "mq_set";
}
