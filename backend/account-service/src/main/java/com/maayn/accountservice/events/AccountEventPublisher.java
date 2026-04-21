package com.maayn.accountservice.events;

import com.maayn.accountservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishAccountCreated(AccountCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ACCOUNT_CREATED_ROUTING_KEY,
                    event);
            log.info("Published AccountCreatedEvent for account: {}", event.getAccountId());
        } catch (Exception e) {
            log.error("Failed to publish AccountCreatedEvent", e);
            // In production, implement retry logic or dead-letter queue
        }
    }

    public void publishAccountClosed(AccountClosedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ACCOUNT_CLOSED_ROUTING_KEY,
                    event);
            log.info("Published AccountClosedEvent for account: {}", event.getAccountId());
        } catch (Exception e) {
            log.error("Failed to publish AccountClosedEvent", e);
        }
    }
}

