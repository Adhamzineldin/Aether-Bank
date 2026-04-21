package com.maayn.transactionservice.config;

import maayn.veld.generated.constants.TransactionRabbitConfig;
import maayn.veld.generated.sdk.audit.constants.AuditRabbitConfig;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    // Core Banking
    public static final String BANKING_EXCHANGE = TransactionRabbitConfig.BANKING_EXCHANGE;
    public static final String ACCOUNT_EVENTS_QUEUE = TransactionRabbitConfig.ACCOUNT_EVENTS_QUEUE;
    public static final String SAGA_COMMANDS_QUEUE = TransactionRabbitConfig.SAGA_COMMANDS_QUEUE;

    /**
     * Routing key account-service publishes with when an account is created.
     * Must stay in sync with {@code account-service}'s publisher.
     */
    public static final String ACCOUNT_CREATED_ROUTING_KEY = "account.created";

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

    /**
     * Wires {@code ACCOUNT_EVENTS_QUEUE} into the banking topic exchange so
     * {@code account.created} events published by account-service are delivered
     * to {@code AccountEventListener}, which seeds the ledger for the new wallet.
     */
    @Bean
    public Binding accountCreatedBinding(Queue accountEventsQueue, TopicExchange bankingExchange) {
        return BindingBuilder.bind(accountEventsQueue)
                .to(bankingExchange)
                .with(ACCOUNT_CREATED_ROUTING_KEY);
    }

    /**
     * Use the listener method's parameter type for deserialization instead of the
     * {@code __TypeId__} header added by the publisher. Senders and receivers
     * across services often have the same event shape under different FQCNs
     * (hand-written class vs Veld-generated SDK class), so inferring the type
     * from the listener signature avoids {@code ClassNotFoundException} on
     * consume.
     */
    @Bean
    public MessageConverter converter() {
        JacksonJsonMessageConverter c = new JacksonJsonMessageConverter();
        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);
        c.setJavaTypeMapper(typeMapper);
        return c;
    }

}