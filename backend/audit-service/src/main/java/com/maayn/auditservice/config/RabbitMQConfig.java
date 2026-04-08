package com.maayn.auditservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String AUDIT_QUEUE = "security_audit_queue";
    public static final String AUDIT_EXCHANGE = "security_audit_exchange";
    public static final String AUDIT_ROUTING_KEY = "audit.log";

    @Bean
    public Queue auditQueue() {
        return new Queue(AUDIT_QUEUE, true); 
    }

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(AUDIT_EXCHANGE);
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange auditExchange) {
        return BindingBuilder.bind(auditQueue).to(auditExchange).with(AUDIT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() {
        return new JacksonJsonMessageConverter();
    }
}