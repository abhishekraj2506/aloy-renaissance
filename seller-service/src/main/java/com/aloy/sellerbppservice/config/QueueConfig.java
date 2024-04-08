package com.aloy.sellerbppservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class QueueConfig {

    @Value("${rabbitmq.input.queue.name}")
    private String inputQueueName;

    @Value("${rabbitmq.output.queue.name}")
    private String outputQueueName;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    // spring bean for rabbitmq queue
    @Bean
    public Queue inputQueue() {
        return new Queue(inputQueueName);
    }

    @Bean
    public Queue outputQueue() {
        return new Queue(outputQueueName);
    }

    // spring bean for rabbitmq exchange
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    Binding binding(Queue outputQueue, DirectExchange exchange) {
        return BindingBuilder.bind(outputQueue).to(exchange).with(routingKey);
    }


}
