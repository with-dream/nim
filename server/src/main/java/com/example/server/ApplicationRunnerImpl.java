package com.example.server;

import com.example.server.entity.MQMapModel;
import com.example.server.netty.NettyServerHandler;
import com.example.server.netty.SessionHolder;
import com.example.server.netty.SessionModel;
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
import java.util.*;

@Component
public class ApplicationRunnerImpl implements ApplicationRunner {
    public static String MQ_NAME = "";
    public static String MQ_TAG = "mq";
    public static String HOST_MAP = "host_map";
    public static String HOST_NAME = "";
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
        HOST_NAME = mqName;

//        test();
        clearMQ();

        Map map = (Map) redisTemplate.opsForHash().entries(ApplicationRunnerImpl.MQ_TAG);
        if (map != null)
            L.e("run map ==>" + map.toString());


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
//        RLock lock = redissonUtil.getLock(cmdMsg.from);
//        lock.lock();
        Set<Object> mqMap = redisTemplate.opsForSet().members(ApplicationRunnerImpl.HOST_NAME);
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

                redisTemplate.opsForSet().remove(ApplicationRunnerImpl.HOST_NAME, item);
            }
        }
//        lock.unlock();
    }
}