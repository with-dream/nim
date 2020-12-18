package com.example.server.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import utils.L;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class DynamicManagerQueueService {
    @Resource
    private AmqpAdmin rabbitAdmin;
    @Resource
    private ConnectionFactory connectionFactory;
    @Resource
    private DynamicListenerService dynamicListenerService;
    @Resource
    private ApplicationContext context;

    public boolean createQueue(QueueDto queueDto) {
        String exchangeName = queueDto.exchange;
        String queueName = queueDto.queueName;
        String routingKey = queueDto.routingKey;
        String listener = queueDto.listener;
        Integer consumers = queueDto.consumers;

        //TODO 如果已存在 则做重新绑定处理
        QueueInformation info = rabbitAdmin.getQueueInfo(queueName);
        if (info != null) {
            return true;
        }

        //实例化一个topic交换机
        Exchange exchange = ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
        //创建队列
        Queue queue = createQueue(queueName, routingKey, exchange);
        MessageListener messageListener = (MessageListener) context.getBean(listener);
        //创建监听者容器
        RabbitListenerContainerFactory rabbitListenerContainerFactory = getRabbitListenerContainerFactory(connectionFactory, consumers == null ? 1 : consumers);
        //监听者监听队列
        dynamicListenerService.listener(queueName, queue, messageListener, rabbitListenerContainerFactory);
        return true;
    }

    /**
     * 创建队列
     *
     * @param queueName  队列名称
     * @param routingKey 路由key
     * @param exchange   交换机
     */
    public Queue createQueue(String queueName, String routingKey, Exchange exchange) {
        //创建交换机
        rabbitAdmin.declareExchange(exchange);
        Queue queue = new Queue(queueName);
        //创建队列
        rabbitAdmin.declareQueue(queue);
        //队列绑定路由key
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs());
        return queue;

    }

    /**
     * 创建队列以及延迟队列绑定
     *
     * @param queueName         队里名称
     * @param routingKey        路由key
     * @param exchange          交换机
     * @param delayQueueName    延迟队列名
     * @param delayQueueRouting 延迟队列路由key
     * @return
     */
    public Queue createDelayQueue(String queueName, String routingKey, Exchange exchange, String delayQueueName, String delayQueueRouting) {

        rabbitAdmin.declareExchange(exchange);
        Queue queue = new Queue(queueName);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs());

        Map<String, Object> delayQueueParams = new HashMap<>(2);
        // DLX，dead letter发送到的exchange
        delayQueueParams.put("x-dead-letter-exchange", exchange.getName());
        // dead letter携带的routing key
        delayQueueParams.put("x-dead-letter-routing-key", routingKey);
        /*
         * 创建一个死信队列
         */
        Queue delayQueue = new Queue(delayQueueName, true, false, false, delayQueueParams);
        rabbitAdmin.declareQueue(delayQueue);
        rabbitAdmin.declareBinding(BindingBuilder.bind(delayQueue).to(exchange).with(delayQueueRouting).and(delayQueueParams));

        return queue;
    }

    public void delQueue(String queueName) {
        rabbitAdmin.deleteQueue(queueName, true, true);
    }

    /**
     * 监听容器大小，多线程还是单线程
     *
     * @param connectionFactory   链接工厂
     * @param concurrentConsumers 线程数
     * @return
     */
    private RabbitListenerContainerFactory getRabbitListenerContainerFactory(ConnectionFactory connectionFactory, Integer concurrentConsumers) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        if (concurrentConsumers > 1) {
            factory.setPrefetchCount(5);
            factory.setConcurrentConsumers(concurrentConsumers);
            factory.setMaxConcurrentConsumers(concurrentConsumers * 2);
        } else {
            factory.setPrefetchCount(1);
            factory.setConcurrentConsumers(1);
            factory.setMaxConcurrentConsumers(1);
        }
        return factory;
    }
}
