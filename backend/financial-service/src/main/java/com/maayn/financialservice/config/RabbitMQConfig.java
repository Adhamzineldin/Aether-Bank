package com.maayn.financialservice.config;

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
    public static final String LOAN_APPROVED_QUEUE = "loan.approved.queue";
    public static final String LOAN_APPROVED_KEY = "loan.approved";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue loanApprovedQueue() {
        return QueueBuilder.durable(LOAN_APPROVED_QUEUE).build();
    }

    @Bean
    public Binding loanApprovedBinding() {
        return BindingBuilder
                .bind(loanApprovedQueue())
                .to(exchange())
                .with(LOAN_APPROVED_KEY);
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

