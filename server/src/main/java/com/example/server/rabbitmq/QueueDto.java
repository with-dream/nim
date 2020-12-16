package com.example.server.rabbitmq;

import java.io.Serializable;

public class QueueDto implements Serializable {
    public String queueName;
    public String exchange;
    public int consumers = 1;
    public String routingKey;
    public String listener;
}
