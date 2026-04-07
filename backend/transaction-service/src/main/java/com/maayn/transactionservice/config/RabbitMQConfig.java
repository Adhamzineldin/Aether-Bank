package com.maayn.transactionservice.config;

import ch.qos.logback.classic.pattern.MessageConverter;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "transaction_exchange";
    public static final String QUEUE = "transaction_notification_queue";
    public static final String ROUTING_KEY = "transaction_routing_key";


    @Bean
    public Queue queue() {
        return new Queue(QUEUE);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }
    
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() {   
        return new MessageConverter();
    }


}
