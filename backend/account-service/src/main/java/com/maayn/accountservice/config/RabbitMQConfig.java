package com.maayn.accountservice.config;

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
    public static final String ACCOUNT_CREATED_QUEUE = "account.created.queue";
    public static final String ACCOUNT_CLOSED_QUEUE = "account.closed.queue";
    public static final String ACCOUNT_CREATED_ROUTING_KEY = "account.created";
    public static final String ACCOUNT_CLOSED_ROUTING_KEY = "account.closed";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue accountCreatedQueue() {
        return QueueBuilder.durable(ACCOUNT_CREATED_QUEUE).build();
    }

    @Bean
    public Queue accountClosedQueue() {
        return QueueBuilder.durable(ACCOUNT_CLOSED_QUEUE).build();
    }

    @Bean
    public Binding accountCreatedBinding() {
        return BindingBuilder
                .bind(accountCreatedQueue())
                .to(exchange())
                .with(ACCOUNT_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding accountClosedBinding() {
        return BindingBuilder
                .bind(accountClosedQueue())
                .to(exchange())
                .with(ACCOUNT_CLOSED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}

