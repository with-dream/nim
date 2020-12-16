package com.example.server.rabbitmq;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class AmqpConfig {

    @Bean("defaultSimpleRabbitListener")
    public SimpleRabbitListenerEndpoint simpleRabbitListenerEndpoint(ConnectionFactory connectionFactory){
        SimpleRabbitListenerEndpoint simpleRabbitListenerEndpoint = new SimpleRabbitListenerEndpoint();
        simpleRabbitListenerEndpoint.setAdmin(rabbitAdmin(connectionFactory));
        return simpleRabbitListenerEndpoint;
    }

    @Bean
    public AmqpAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

}
