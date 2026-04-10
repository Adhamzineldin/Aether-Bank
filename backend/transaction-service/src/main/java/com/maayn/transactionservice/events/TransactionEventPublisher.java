package com.maayn.transactionservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maayn.transactionservice.config.RabbitMQConfig;
import com.maayn.transactionservice.entity.OutboxMessage;
import com.maayn.transactionservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
        saveToOutbox(RabbitMQConfig.TRANSACTION_EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
    }

    public void publish(TransferFailedEvent event) {
        saveToOutbox(RabbitMQConfig.TRANSACTION_EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
    }

    @SneakyThrows
    private void saveToOutbox(String exchange, String routingKey, Object eventPayload) {
        String jsonPayload = objectMapper.writeValueAsString(eventPayload);

        OutboxMessage outboxMessage = new OutboxMessage(exchange, routingKey, jsonPayload);
        outboxRepository.save(outboxMessage);

        log.info("Event safely stored in Outbox for routing key: {}", routingKey);
    }
}