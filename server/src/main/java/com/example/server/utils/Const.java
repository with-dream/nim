package com.example.server.utils;

import utils.Constant;

public class Const {
    public static String MQ_TAG = "mq";
    private static String res[] = new String[Constant.clientType.length];

    /**
     * 用于标识redis的MQMapModel 使用客户端类型做后缀 唯一标识一个客户端类型
     */
    public static String mqTag(int type) {
        return MQ_TAG + Constant.mapDevice(type);
    }

    public static String[] mqTagList() {
        if (res.length == 0) {
            synchronized (res) {
                if (res.length == 0) {
                    int index = 0;
                    for (int t : Constant.clientType)
                        res[index++] = mqTag(t);
                }
            }
        }

        return res;
    }

}
