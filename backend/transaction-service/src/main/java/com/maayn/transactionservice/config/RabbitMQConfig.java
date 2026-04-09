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
    // SAGA Constants
    public static final String SAGA_EXCHANGE = "bank_saga_exchange";
    public static final String TRANSFER_INITIATED_ROUTING_KEY = "saga.transfer.initiated";
    public static final String SAGA_SUCCESS_QUEUE = "saga_success_queue";
    public static final String SAGA_FAILURE_QUEUE = "saga_failure_queue";

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

    // --- SAGA Configuration Beans ---

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SAGA_EXCHANGE);
    }

    @Bean
    public Queue sagaSuccessQueue() {
        return new Queue(SAGA_SUCCESS_QUEUE, true);
    }

    @Bean
    public Queue sagaFailureQueue() {
        return new Queue(SAGA_FAILURE_QUEUE, true);
    }

    @Bean
    public Binding successBinding(Queue sagaSuccessQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(sagaSuccessQueue).to(sagaExchange).with("saga.transfer.success");
    }

    @Bean
    public Binding failureBinding(Queue sagaFailureQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(sagaFailureQueue).to(sagaExchange).with("saga.transfer.failure");
    }
}