package com.example.server;

import com.example.server.rabbitmq.DynamicManagerQueueService;
import com.example.server.rabbitmq.QueueDto;
import com.example.server.rabbitmq.RabbitListener;
import com.example.server.zookeeper.ZooUtil;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import utils.L;

import javax.annotation.Resource;

@Component
public class ApplicationRunnerImpl implements ApplicationRunner {
    public static String MQ_NAME = "";
    public static String MQ_TAG = "mq";
    private Gson gson = new Gson();
    @Resource
    DynamicManagerQueueService queueService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String mqName = null;
        for (String str : args.getNonOptionArgs()) {
            String[] ss = str.split("=");
            switch (ss[0]) {
                case "mq":
                    mqName = ss[1];
                    break;
            }
            L.p("str3==>" + str);
        }

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
}