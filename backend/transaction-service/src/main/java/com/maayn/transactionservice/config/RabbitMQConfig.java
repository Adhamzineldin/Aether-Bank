package com.maayn.transactionservice.config;


import maayn.veld.generated.constants.TransactionRabbitConfig;
import maayn.veld.generated.sdk.audit.constants.AuditRabbitConfig;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String ROUTING_KEY = TransactionRabbitConfig.ROUTING_KEY;
    public static final String TRANSACTION_EXCHANGE = TransactionRabbitConfig.TRANSACTION_EXCHANGE;
    public static final String TRANSACTION_QUEUE = TransactionRabbitConfig.TRANSACTION_QUEUE;
    
    public static final String AUDIT_EXCHANGE = AuditRabbitConfig.AUDIT_EXCHANGE;
    public static final String AUDIT_ROUTING_KEY = AuditRabbitConfig.AUDIT_ROUTING_KEY;
    
    
    // SAGA Constants
    public static final String SAGA_EXCHANGE = TransactionRabbitConfig.SAGA_EXCHANGE;
    public static final String TRANSFER_INITIATED_ROUTING_KEY = TransactionRabbitConfig.TRANSFER_INITIATED_ROUTING_KEY;
    public static final String SAGA_SUCCESS_QUEUE = TransactionRabbitConfig.SAGA_SUCCESS_QUEUE;
    public static final String SAGA_FAILURE_QUEUE =  TransactionRabbitConfig.SAGA_FAILURE_QUEUE;

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