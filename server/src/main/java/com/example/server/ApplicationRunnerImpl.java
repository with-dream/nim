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
import org.springframework.data.redis.core.ListOperations;
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
        redisTemplate.delete("111");

        ListOperations<String, Object> listVo = redisTemplate.opsForList();
        listVo.rightPush("111", "111");
        listVo.rightPush("111", "222");
        listVo.rightPush("111", "333");
        listVo.rightPush("111", "444");
        L.e("==>" + listVo.rightPush("111", "555"));
        listVo.remove("111", 1, "111");
        listVo.remove("111", 1, "222");

        L.e("==>" + listVo.index("111", 0));
        L.e("==>" + listVo.index("111", 2));
        Long iii = listVo.indexOf("111", "2");
        L.e("==>" + iii);
        L.e("==>" + listVo.indexOf("111", "555"));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

//        test();
        Map map = (Map) redisTemplate.opsForHash().entries(ApplicationRunnerImpl.MQ_TAG);
        if (map != null)
            L.e("run map ==>" + map.toString());

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