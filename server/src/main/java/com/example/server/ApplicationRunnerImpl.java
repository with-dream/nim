package com.example.server;

import com.example.server.netty.SendHolder;
import com.example.server.netty.entity.SessionRedisEntity;
import com.example.server.rabbitmq.DynamicManagerQueueService;
import com.example.server.rabbitmq.QueueDto;
import com.example.server.rabbitmq.RabbitListener;
import com.example.server.redis.RConst;
import org.redisson.api.RList;
import org.redisson.api.RSet;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
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
    RedissonClient redisson;

    @Override
    public void run(ApplicationArguments args) {
//        String mqName = null;
//        for (String str : args.getNonOptionArgs()) {
//            String[] s = str.split("&");
//            for (String tmp : s) {
//                String[] ss = str.split("=");
//                switch (ss[0]) {
//                    case "mq":
//                        mqName = ss[1];
//                        break;
//                }
//                L.p("str3==>" + str);
//            }
//        }
//        MQ_NAME = mqName;

        clearMQ();

        QueueDto queueDto = new QueueDto();
        queueDto.queueName = MQ_NAME;
        queueDto.exchange = "exchange";
        queueDto.routingKey = "routingKey";
        queueDto.listener = RabbitListener.LISTENER_TAG;

        if (queueService.createQueue(queueDto)) {
//            MQ_NAME = mqName;
            RSet<String> mqList = redisson.getSet(RConst.MQ_SET);
            mqList.add(MQ_NAME);
        } else {
            throw new RuntimeException("==>创建mq失败");
        }
    }

    //重新绑定mq 需要将redis中的缓存数据清除
    private void clearMQ() {
        RSetMultimap<String, SessionRedisEntity> multimap = redisson.getSetMultimap(RConst.UUID_MQ_MAP);
        if (multimap.isEmpty()) return;
        Set<Map.Entry<String, SessionRedisEntity>> setEntry = multimap.entries();
        if (setEntry.isEmpty()) return;
        setEntry.removeIf(entry -> entry.getValue().queueName.equals(MQ_NAME));
    }
}