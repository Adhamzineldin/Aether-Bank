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
 * {@link com.maayn.notificationservice.events.SubmittedApplicationWorkflowListener}.
 *
 * The account-created / transaction-success queues that used to live here have
 * been removed: account lifecycle events now flow through {@code banking.exchange}
 * (see {@code com.maayn.notificationservice.config.rabbit.RabbitMQConfig}) and
 * transfer notifications are served by {@code TransferEventListener}.
 */
@Configuration("legacyBankEventsConfig")
public class RabbitMQConfig {

    public static final String EXCHANGE = "bank.events";

    public static final String LOAN_SUBMITTED_QUEUE = "loan.submitted.queue";
    public static final String CERTIFICATE_SUBMITTED_QUEUE = "certificate.submitted.queue";
    public static final String MORTGAGE_SUBMITTED_QUEUE = "mortgage.submitted.queue";

    public static final String LOAN_SUBMITTED_KEY = "loan.submitted";
    public static final String CERTIFICATE_SUBMITTED_KEY = "certificate.submitted";
    public static final String MORTGAGE_SUBMITTED_KEY = "mortgage.submitted";

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

    @Bean
    public Queue certificateSubmittedQueue() {
        return QueueBuilder.durable(CERTIFICATE_SUBMITTED_QUEUE).build();
    }

    @Bean
    public Binding certificateSubmittedBinding() {
        return BindingBuilder.bind(certificateSubmittedQueue())
                .to(exchange())
                .with(CERTIFICATE_SUBMITTED_KEY);
    }

    @Bean
    public Queue mortgageSubmittedQueue() {
        return QueueBuilder.durable(MORTGAGE_SUBMITTED_QUEUE).build();
    }

    @Bean
    public Binding mortgageSubmittedBinding() {
        return BindingBuilder.bind(mortgageSubmittedQueue())
                .to(exchange())
                .with(MORTGAGE_SUBMITTED_KEY);
    }
}
