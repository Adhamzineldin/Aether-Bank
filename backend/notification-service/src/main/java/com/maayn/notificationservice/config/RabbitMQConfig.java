package com.maayn.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "bank.events";
    
    // Queues
    public static final String LOAN_SUBMITTED_QUEUE = "loan.submitted.queue";
    public static final String TRANSACTION_SUCCESS_QUEUE = "transaction.success.queue";
    public static final String ACCOUNT_CREATED_QUEUE = "account.created.queue";
    
    // Routing keys
    public static final String LOAN_SUBMITTED_KEY = "loan.submitted";
    public static final String TRANSACTION_SUCCESS_KEY = "transaction.success";
    public static final String ACCOUNT_CREATED_KEY = "account.created";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue loanSubmittedQueue() {
        return QueueBuilder.durable(LOAN_SUBMITTED_QUEUE).build();
    }

    @Bean
    public Queue transactionSuccessQueue() {
        return QueueBuilder.durable(TRANSACTION_SUCCESS_QUEUE).build();
    }

    @Bean
    public Queue accountCreatedQueue() {
        return QueueBuilder.durable(ACCOUNT_CREATED_QUEUE).build();
    }

    @Bean
    public Binding loanSubmittedBinding() {
        return BindingBuilder.bind(loanSubmittedQueue())
                .to(exchange())
                .with(LOAN_SUBMITTED_KEY);
    }

    @Bean
    public Binding transactionSuccessBinding() {
        return BindingBuilder.bind(transactionSuccessQueue())
                .to(exchange())
                .with(TRANSACTION_SUCCESS_KEY);
    }

    @Bean
    public Binding accountCreatedBinding() {
        return BindingBuilder.bind(accountCreatedQueue())
                .to(exchange())
                .with(ACCOUNT_CREATED_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

