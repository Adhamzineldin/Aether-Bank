package com.maayn.accountservice.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    
    private static final String EXCHANGE = "bank.events";
    private static final String ACCOUNT_CREATED_ROUTING_KEY = "account.created";
    private static final String ACCOUNT_CLOSED_ROUTING_KEY = "account.closed";

    public void publishAccountCreated(AccountCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ACCOUNT_CREATED_ROUTING_KEY, event);
            log.info("Published AccountCreatedEvent for account: {}", event.getAccountId());
        } catch (Exception e) {
            log.error("Failed to publish AccountCreatedEvent", e);
            // In production, implement retry logic or dead-letter queue
        }
    }

    public void publishAccountClosed(AccountClosedEvent event) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ACCOUNT_CLOSED_ROUTING_KEY, event);
            log.info("Published AccountClosedEvent for account: {}", event.getAccountId());
        } catch (Exception e) {
            log.error("Failed to publish AccountClosedEvent", e);
        }
    }
}

