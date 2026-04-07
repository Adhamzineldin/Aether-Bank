package com.maayn.transactionservice.events;

import com.maayn.transactionservice.config.RabbitMQConfig;
import com.maayn.transactionservice.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.TransactionEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(Transaction saved) {
        try {
            TransactionEvent event = new TransactionEvent(
                    saved.getReferenceNumber(),
                    saved.getSourceAccountId(),
                    saved.getDestinationAccountId(),
                    saved.getAmount(),
                    saved.getStatus()
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    event
            );
            log.info("Successfully published event for transaction: {}", saved.getReferenceNumber());
        } catch (Exception e) {
            log.error("Failed to publish transaction event for {}: {}", saved.getReferenceNumber(), e.getMessage());
        }
    }
}