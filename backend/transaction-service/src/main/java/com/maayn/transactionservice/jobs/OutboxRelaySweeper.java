package com.maayn.transactionservice.jobs;

import com.maayn.transactionservice.entity.OutboxMessage;
import com.maayn.transactionservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelaySweeper {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    
    
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void relayOutboxMessages() {
        List<OutboxMessage> messages = fetchPendingMessages();

        if (messages.isEmpty()) return;

        log.info("Outbox Sweeper found {} messages to relay.", messages.size());
        processBatch(messages);
    }
    
    private List<OutboxMessage> fetchPendingMessages() {
        return outboxRepository.findAllByOrderByCreatedAtAsc();
    }
    
    private void processBatch(List<OutboxMessage> messages) {
        for (OutboxMessage message : messages) {
            boolean success = attemptRelay(message);
            if (!success) {
                log.warn("Halting outbox batch processing. Will retry remaining messages next cycle.");
                break;
            }
        }
    }
    
    private boolean attemptRelay(OutboxMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    message.getExchange(),
                    message.getRoutingKey(),
                    message.getPayload()
            );

            outboxRepository.delete(message);
            return true;

        } catch (Exception e) {
            log.error("Failed to relay Outbox message {}. RabbitMQ might be down: {}", message.getId(), e.getMessage());
            return false;
        }
    }
}