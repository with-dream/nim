package com.example.server.utils;

import org.apache.commons.lang.StringUtils;
import utils.Constant;

public class Const {
    public static String MQ_TAG = "mq";
    private static String res[] = new String[Constant.clientType.length];

    /**
     * 用于标识redis的MQMapModel 使用客户端类型做后缀 唯一标识一个客户端类型
     */
    public static String mqTag(int deviceType) {
        return MQ_TAG + Constant.mapDevice(deviceType);
    }

    public static String[] mqTagList() {
        if (StringUtils.isEmpty(res[0])) {
            synchronized (res) {
                if (StringUtils.isEmpty(res[0]))
                    for (int i = 0; i < Constant.clientType.length; i++)
                        res[i] = MQ_TAG + i;
            }
        }

        return res;
    }

}
