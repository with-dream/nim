package com.example.server;

import com.example.server.netty.SessionHolder;
import com.example.server.netty.SessionModel;
import com.example.server.rabbitmq.DynamicManagerQueueService;
import com.example.server.rabbitmq.QueueDto;
import com.example.server.rabbitmq.RabbitListener;
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

    private void clearMQ() {
        Set<Object> mqMap = redisTemplate.opsForSet().members(ApplicationRunnerImpl.MQ_NAME);
        if (mqMap != null && !mqMap.isEmpty()) {
            for (Object item : mqMap) {
                String[] uuidToken = ((String) item).split(":");

                boolean flag = false;
                Vector<SessionModel> sess = SessionHolder.sessionMap.get(uuidToken[0]);
                if (sess != null && !sess.isEmpty())
                    for (SessionModel model : sess)
                        if (model.clientToken == Integer.parseInt(uuidToken[1])) {
                            flag = true;
                            break;
                        }
                if (flag)
                    continue;

                Map<Integer, MQMapModel> map = (Map) redisTemplate.opsForHash().get(ApplicationRunnerImpl.MQ_TAG, uuidToken[0]);
                if (map != null) {
                    map.remove(Integer.parseInt(uuidToken[1]));
                }

                if (map == null || map.isEmpty())
                    redisTemplate.opsForHash().delete(ApplicationRunnerImpl.MQ_TAG, uuidToken[0]);
                else
                    redisTemplate.opsForHash().put(ApplicationRunnerImpl.MQ_TAG, uuidToken[0], map);

                redisTemplate.opsForSet().remove(ApplicationRunnerImpl.MQ_NAME, item);
            }
        }
    }
}