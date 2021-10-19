package com.example.server;

import com.example.server.rabbitmq.DynamicManagerQueueService;
import com.example.server.rabbitmq.QueueDto;
import com.example.server.rabbitmq.RabbitListener;
import com.example.server.utils.Const;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import utils.L;

import javax.annotation.Resource;
import java.util.*;

@Component
public class ApplicationRunnerImpl implements ApplicationRunner {
    public static String MQ_NAME = "";

    @Resource
    DynamicManagerQueueService queueService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        String mqName = null;
        for (String str : args.getNonOptionArgs()) {
            String[] s = str.split("&");
            for (String tmp : s) {
                String[] ss = str.split("=");
                switch (ss[0]) {
                    case "mq":
                        mqName = ss[1];
                        break;
                }
                L.p("str3==>" + str);
            }
        }
        MQ_NAME = mqName;

        clearMQ();

        QueueDto queueDto = new QueueDto();
        queueDto.queueName = mqName;
        queueDto.exchange = "exchange";
        queueDto.routingKey = "routingKey";
        queueDto.listener = RabbitListener.LISTENER_TAG;

        if (queueService.createQueue(queueDto)) {
            MQ_NAME = mqName;
        } else {
            throw new RuntimeException("==>创建mq失败");
        }
    }

    //重新绑定mq 需要将redis中的缓存数据清除
    private void clearMQ() {
        Set<Object> cacheUUid = redisTemplate.opsForSet().members(ApplicationRunnerImpl.MQ_NAME);
        if (cacheUUid != null && !cacheUUid.isEmpty()) {
            for (Object obj : cacheUUid) {
                String[] item = ((String) obj).split(":");
                String uuid = item[0];
                int deviceType = Integer.valueOf(item[1]);
                redisTemplate.opsForHash().delete(Const.mqTag(deviceType), uuid);
            }
        }
    }
}