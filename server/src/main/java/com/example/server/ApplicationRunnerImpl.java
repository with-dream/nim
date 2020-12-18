package com.example.server;

import com.example.server.entity.MQMapModel;
import com.example.server.rabbitmq.DynamicManagerQueueService;
import com.example.server.rabbitmq.QueueDto;
import com.example.server.rabbitmq.RabbitListener;
import com.example.server.zookeeper.ZooUtil;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import utils.L;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApplicationRunnerImpl implements ApplicationRunner {
    public static String MQ_NAME = "";
    public static String MQ_TAG = "mq";
    private Gson gson = new Gson();
    @Resource
    DynamicManagerQueueService queueService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private void test() {
        HashOperations<String, String, Object> hashVo = redisTemplate.opsForHash();
        Map<String, String> mm = new HashMap<>();
        mm.put("111", "aaa");
        mm.put("222", "bbb");
        hashVo.put(ApplicationRunnerImpl.MQ_TAG, "abc", mm);
        mm.put("333", "ccc");
        Map<String, String> value = (Map<String, String>) hashVo.get(ApplicationRunnerImpl.MQ_TAG, "abc");
        L.e("==>" + value.toString());
        L.e("==>" + hashVo.entries(ApplicationRunnerImpl.MQ_TAG));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

//        test();

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