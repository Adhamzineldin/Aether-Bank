package com.maayn.iamservice.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * iam-service publishes security audit events to {@code security_audit_exchange}
 * via {@link com.maayn.iamservice.audit.AuditPublisher}. Without an explicit
 * {@link MessageConverter}, Spring Boot's {@code RabbitAutoConfiguration}
 * falls back to {@code SimpleMessageConverter} which uses Java serialisation
 * — and audit-service's {@link JacksonJsonMessageConverter} can't read that,
 * so every iam audit event was silently being dropped on consume.
 *
 * <p>This config swaps the converter to JSON so audit events arrive on the
 * wire as plain JSON objects that any consumer can decode.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}

