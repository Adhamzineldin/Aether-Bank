package com.maayn.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Legacy event plumbing for the loan approval flow.
 *
 * The {@code bank.events} exchange is retained because {@code financial-service}
 * still publishes {@code loan.submitted}, {@code loan.disbursed} and
 * {@code certificate.submitted} events there and they are consumed by
 * {@link com.maayn.notificationservice.events.LoanApplicationListener}.
 *
 * The account-created / transaction-success queues that used to live here have
 * been removed: account lifecycle events now flow through {@code banking.exchange}
 * (see {@code com.maayn.notificationservice.config.rabbit.RabbitMQConfig}) and
 * transfer notifications are served by {@code TransferEventListener}.
 */
@Configuration("legacyBankEventsConfig")
public class RabbitMQConfig {

    public static final String EXCHANGE = "bank.events";

    // Queues
    public static final String LOAN_SUBMITTED_QUEUE = "loan.submitted.queue";

    // Routing keys
    public static final String LOAN_SUBMITTED_KEY = "loan.submitted";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue loanSubmittedQueue() {
        return QueueBuilder.durable(LOAN_SUBMITTED_QUEUE).build();
    }

    @Bean
    public Binding loanSubmittedBinding() {
        return BindingBuilder.bind(loanSubmittedQueue())
                .to(exchange())
                .with(LOAN_SUBMITTED_KEY);
    }
}
