package com.maayn.transactionservice.config;

import maayn.veld.generated.constants.TransactionRabbitConfig;
import maayn.veld.generated.sdk.audit.constants.AuditRabbitConfig;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;


@Configuration
public class RabbitMQConfig {

    // Core Banking
    public static final String BANKING_EXCHANGE = TransactionRabbitConfig.BANKING_EXCHANGE;
    public static final String ACCOUNT_EVENTS_QUEUE = TransactionRabbitConfig.ACCOUNT_EVENTS_QUEUE;
    public static final String SAGA_COMMANDS_QUEUE = TransactionRabbitConfig.SAGA_COMMANDS_QUEUE;

    // Audit
    public static final String AUDIT_EXCHANGE = AuditRabbitConfig.AUDIT_EXCHANGE;
    public static final String AUDIT_ROUTING_KEY = AuditRabbitConfig.AUDIT_ROUTING_KEY;


    @Bean
    public TopicExchange bankingExchange() {
        return new TopicExchange(BANKING_EXCHANGE);
    }

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(AUDIT_EXCHANGE);
    }


    @Bean
    public Queue accountEventsQueue() {
        return new Queue(ACCOUNT_EVENTS_QUEUE, true); // true = durable (survives broker restart)
    }

    @Bean
    public Queue sagaCommandsQueue() {
        return new Queue(SAGA_COMMANDS_QUEUE, true);
    }

    @Bean
    public MessageConverter converter() {
        return new JacksonJsonMessageConverter();
    }

}