package com.maayn.transactionservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String ROUTING_KEY = "transaction_routing_key";
    public static final String TRANSACTION_EXCHANGE = "transaction_exchange";
    public static final String TRANSACTION_QUEUE = "transaction_notification_queue";
    public static final String AUDIT_EXCHANGE = "security_audit_exchange";
    public static final String AUDIT_ROUTING_KEY = "audit.log";

    @Bean
    public Queue queue() {
        return new Queue(TRANSACTION_QUEUE);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(AUDIT_EXCHANGE);
    }
}