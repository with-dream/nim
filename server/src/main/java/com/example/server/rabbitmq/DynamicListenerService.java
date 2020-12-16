package com.example.server.rabbitmq;

import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DynamicListenerService {
    @Resource
    @Qualifier("defaultSimpleRabbitListener")
    private SimpleRabbitListenerEndpoint simpleRabbitListenerEndpoint;

    @Resource
    private BeanFactory beanFactory;

    @Resource
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    public void listener(String queueName, Queue queue, MessageListener messageListener, RabbitListenerContainerFactory rabbitListenerContainerFactory) {
        simpleRabbitListenerEndpoint.setMessageListener(messageListener);
        simpleRabbitListenerEndpoint.setBeanFactory(this.beanFactory);
        simpleRabbitListenerEndpoint.setQueues(queue);
        simpleRabbitListenerEndpoint.setId(queueName);
        this.rabbitListenerEndpointRegistry.registerListenerContainer(simpleRabbitListenerEndpoint, rabbitListenerContainerFactory, true);
    }
}
