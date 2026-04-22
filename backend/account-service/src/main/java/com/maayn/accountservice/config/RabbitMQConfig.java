package com.maayn.accountservice.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * account-service is a <em>publisher</em> on the banking topic. Consumer queues
 * and bindings are owned by the services that actually consume those routing
 * keys (transaction-service for {@code account.created} ledger seeding,
 * notification-service for customer-facing notifications, etc.).
 *
 * We therefore only declare:
 * <ul>
 *   <li>the shared {@code banking.exchange} topic exchange,</li>
 *   <li>a {@link JacksonJsonMessageConverter} that writes JSON without a
 *       {@code __TypeId__} header (consumers infer the target type),</li>
 *   <li>a {@link RabbitTemplate} wired to that converter.</li>
 * </ul>
 *
 * Anything else (queues, bindings) would create dangling infrastructure that no
 * account-service process ever reads from, which is how we ended up with a
 * residual {@code account.created.queue} in the past.
 */
@Configuration
public class RabbitMQConfig {

    /** Must match {@code maayn.veld.generated.constants.TransactionRabbitConfig#BANKING_EXCHANGE}. */
    public static final String EXCHANGE = "banking.exchange";

    public static final String ACCOUNT_CREATED_ROUTING_KEY = "account.created";
    public static final String ACCOUNT_CLOSED_ROUTING_KEY = "account.closed";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
