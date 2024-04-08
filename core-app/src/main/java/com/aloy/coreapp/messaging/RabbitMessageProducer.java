package com.aloy.coreapp.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    private static final Long DEFAULT_DELAY_IN_MILLIS = 60000L;
    private static final boolean publish = true;


    public void sendMessage(String message) {
        if (publish) {
            log.info("Sending rabbitmq message " + message);
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        } else {
            log.info("Skipping publish to queue");
        }

    }
}
