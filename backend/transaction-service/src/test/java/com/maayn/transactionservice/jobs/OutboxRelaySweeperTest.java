package com.maayn.transactionservice.jobs;

import com.maayn.transactionservice.entity.OutboxMessage;
import com.maayn.transactionservice.repository.OutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxRelaySweeper Unit Tests")
class OutboxRelaySweeperTest {

    @Mock private OutboxRepository outboxRepository;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks private OutboxRelaySweeper sweeper;

    private OutboxMessage createOutboxMessage(String exchange, String routingKey, String payload) {
        OutboxMessage msg = new OutboxMessage(exchange, routingKey, payload);
        // Simulate a persisted message
        return msg;
    }

    // ════════════════════════════════════════════════════════════════
    //  relayOutboxMessages()
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("relayOutboxMessages()")
    class RelayTests {

        @Test
        @DisplayName("Should do nothing when outbox is empty")
        void relayOutboxMessages_empty_doesNothing() {
            when(outboxRepository.findAllByOrderByCreatedAtAsc()).thenReturn(Collections.emptyList());

            sweeper.relayOutboxMessages();

            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
            verify(outboxRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should relay single message and delete it from outbox")
        void relayOutboxMessages_singleMessage_relaysAndDeletes() {
            OutboxMessage msg = createOutboxMessage("banking.exchange", "transaction.transfer.success", "{\"ref\":\"TXN-01\"}");

            when(outboxRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(msg));

            sweeper.relayOutboxMessages();

            verify(rabbitTemplate).convertAndSend("banking.exchange", "transaction.transfer.success", "{\"ref\":\"TXN-01\"}");
            verify(outboxRepository).delete(msg);
        }

        @Test
        @DisplayName("Should relay all messages in order and delete each")
        void relayOutboxMessages_multipleMessages_relaysAll() {
            OutboxMessage msg1 = createOutboxMessage("exchange1", "key1", "payload1");
            OutboxMessage msg2 = createOutboxMessage("exchange2", "key2", "payload2");
            OutboxMessage msg3 = createOutboxMessage("exchange3", "key3", "payload3");

            when(outboxRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(msg1, msg2, msg3));

            sweeper.relayOutboxMessages();

            verify(rabbitTemplate).convertAndSend("exchange1", "key1", "payload1");
            verify(rabbitTemplate).convertAndSend("exchange2", "key2", "payload2");
            verify(rabbitTemplate).convertAndSend("exchange3", "key3", "payload3");
            verify(outboxRepository, times(3)).delete(any());
        }

        @Test
        @DisplayName("Should halt batch processing when RabbitMQ is down")
        void relayOutboxMessages_rabbitDown_haltsBatch() {
            OutboxMessage msg1 = createOutboxMessage("exchange", "key1", "payload1");
            OutboxMessage msg2 = createOutboxMessage("exchange", "key2", "payload2");

            when(outboxRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(msg1, msg2));

            doThrow(new RuntimeException("RabbitMQ connection refused"))
                    .when(rabbitTemplate).convertAndSend(eq("exchange"), eq("key1"), eq("payload1"));

            sweeper.relayOutboxMessages();

            // First message fails, second should NOT be attempted
            verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyString());
            verify(outboxRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should continue relaying after successful first message, halt on second failure")
        void relayOutboxMessages_failsOnSecond_relaysFirstOnly() {
            OutboxMessage msg1 = createOutboxMessage("exchange", "key1", "payload1");
            OutboxMessage msg2 = createOutboxMessage("exchange", "key2", "payload2");
            OutboxMessage msg3 = createOutboxMessage("exchange", "key3", "payload3");

            when(outboxRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(msg1, msg2, msg3));

            // First succeeds, second fails
            doNothing().when(rabbitTemplate).convertAndSend("exchange", "key1", "payload1");
            doThrow(new RuntimeException("Connection lost"))
                    .when(rabbitTemplate).convertAndSend("exchange", "key2", "payload2");

            sweeper.relayOutboxMessages();

            verify(outboxRepository, times(1)).delete(msg1); // Only first deleted
            verify(outboxRepository, never()).delete(msg2);
            verify(outboxRepository, never()).delete(msg3);
            verify(rabbitTemplate, never()).convertAndSend("exchange", "key3", "payload3"); // Third not attempted
        }

        @Test
        @DisplayName("Should query outbox in ascending createdAt order")
        void relayOutboxMessages_queriesInOrder() {
            when(outboxRepository.findAllByOrderByCreatedAtAsc()).thenReturn(Collections.emptyList());

            sweeper.relayOutboxMessages();

            verify(outboxRepository).findAllByOrderByCreatedAtAsc();
        }
    }
}

