package com.maayn.transactionservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maayn.transactionservice.entity.OutboxMessage;
import com.maayn.transactionservice.repository.OutboxRepository;
import maayn.veld.generated.constants.TransactionRabbitConfig;
import maayn.veld.generated.sdk.notification.models.shared.TransferFailedEvent;
import maayn.veld.generated.sdk.notification.models.shared.TransferSuccessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionEventPublisher Unit Tests")
class TransactionEventPublisherTest {

    @Mock private OutboxRepository outboxRepository;
    @Spy  private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks private TransactionEventPublisher publisher;

    // ════════════════════════════════════════════════════════════════
    //  publish(TransferSuccessEvent)
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("publish(TransferSuccessEvent)")
    class PublishSuccessEventTests {

        @Test
        @DisplayName("Should save success event to outbox with correct exchange and routing key")
        void publish_successEvent_savesToOutbox() {
            TransferSuccessEvent event = new TransferSuccessEvent(
                    "TXN-TEST01",
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    new BigDecimal("100.00"),
                    "USD",
                    LocalDateTime.now()
            );

            publisher.publish(event);

            ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessage saved = captor.getValue();
            assertThat(saved.getExchange()).isEqualTo(TransactionRabbitConfig.BANKING_EXCHANGE);
            assertThat(saved.getRoutingKey()).isEqualTo(TransactionRabbitConfig.TRANSFER_SUCCESS_ROUTING_KEY);
            assertThat(saved.getPayload()).isNotBlank();
            assertThat(saved.getPayload()).contains("TXN-TEST01");
        }

        @Test
        @DisplayName("Should serialize event payload as JSON containing all fields")
        void publish_successEvent_serializesCorrectly() {
            UUID srcId = UUID.randomUUID();
            UUID destId = UUID.randomUUID();

            TransferSuccessEvent event = new TransferSuccessEvent(
                    "TXN-JSON01", srcId, destId, new BigDecimal("250.50"), "EUR", LocalDateTime.now()
            );

            publisher.publish(event);

            ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
            verify(outboxRepository).save(captor.capture());

            String payload = captor.getValue().getPayload();
            assertThat(payload).contains("referenceNumber");
            assertThat(payload).contains("TXN-JSON01");
            assertThat(payload).contains(srcId.toString());
            assertThat(payload).contains(destId.toString());
            assertThat(payload).contains("250.50");
            assertThat(payload).contains("EUR");
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  publish(TransferFailedEvent)
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("publish(TransferFailedEvent)")
    class PublishFailedEventTests {

        @Test
        @DisplayName("Should save failed event to outbox with correct routing key")
        void publish_failedEvent_savesToOutbox() {
            TransferFailedEvent event = new TransferFailedEvent();
            event.setReferenceNumber("TXN-FAIL01");
            event.setSourceAccountId(UUID.randomUUID());
            event.setDestinationAccountId(UUID.randomUUID());
            event.setAmount(new BigDecimal("500.00"));
            event.setCurrency("USD");
            event.setFailureReason("Insufficient funds");
            event.setEventTime(LocalDateTime.now());

            publisher.publish(event);

            ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessage saved = captor.getValue();
            assertThat(saved.getExchange()).isEqualTo(TransactionRabbitConfig.BANKING_EXCHANGE);
            assertThat(saved.getRoutingKey()).isEqualTo(TransactionRabbitConfig.TRANSFER_FAILED_ROUTING_KEY);
            assertThat(saved.getPayload()).contains("Insufficient funds");
            assertThat(saved.getPayload()).contains("TXN-FAIL01");
        }

        @Test
        @DisplayName("Should include failure reason in serialized payload")
        void publish_failedEvent_includesFailureReason() {
            TransferFailedEvent event = new TransferFailedEvent();
            event.setReferenceNumber("TXN-FAIL02");
            event.setSourceAccountId(UUID.randomUUID());
            event.setDestinationAccountId(UUID.randomUUID());
            event.setAmount(new BigDecimal("100.00"));
            event.setCurrency("USD");
            event.setFailureReason("Account not found");
            event.setEventTime(LocalDateTime.now());

            publisher.publish(event);

            ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
            verify(outboxRepository).save(captor.capture());

            assertThat(captor.getValue().getPayload()).contains("Account not found");
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Outbox createdAt
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Outbox Message Properties")
    class OutboxMessagePropertiesTests {

        @Test
        @DisplayName("Should create outbox message with a non-null createdAt timestamp")
        void outboxMessage_hasCreatedAt() {
            TransferSuccessEvent event = new TransferSuccessEvent(
                    "TXN-TS01", UUID.randomUUID(), UUID.randomUUID(),
                    new BigDecimal("10.00"), "USD", LocalDateTime.now()
            );

            publisher.publish(event);

            ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
            verify(outboxRepository).save(captor.capture());

            assertThat(captor.getValue().getCreatedAt()).isNotNull();
        }
    }
}

