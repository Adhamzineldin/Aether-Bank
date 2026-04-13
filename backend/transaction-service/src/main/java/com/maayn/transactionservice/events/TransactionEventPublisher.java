package com.maayn.transactionservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maayn.transactionservice.entity.OutboxMessage;
import com.maayn.transactionservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// REMOVE THIS: import lombok.SneakyThrows;
import maayn.veld.generated.constants.TransactionRabbitConfig;
import maayn.veld.generated.sdk.notification.models.shared.TransferFailedEvent;
import maayn.veld.generated.sdk.notification.models.shared.TransferSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventPublisher {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void publish(TransferSuccessEvent event) {
        saveToOutbox(
                TransactionRabbitConfig.BANKING_EXCHANGE,
                TransactionRabbitConfig.TRANSFER_SUCCESS_ROUTING_KEY,
                event
        );
    }

    public void publish(TransferFailedEvent event) {
        saveToOutbox(
                TransactionRabbitConfig.BANKING_EXCHANGE,
                TransactionRabbitConfig.TRANSFER_FAILED_ROUTING_KEY,
                event
        );
    }

    private void saveToOutbox(String exchange, String routingKey, Object eventPayload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(eventPayload);

            OutboxMessage outboxMessage = new OutboxMessage(exchange, routingKey, jsonPayload);
            outboxRepository.save(outboxMessage);

            log.info("Event safely stored in Outbox for routing key: {}", routingKey);
        } catch (Exception e) {
            log.error("Failed to serialize Outbox event: {}", e.getMessage());
            throw new RuntimeException("JSON serialization failed for Outbox message", e);
        }
    }
}