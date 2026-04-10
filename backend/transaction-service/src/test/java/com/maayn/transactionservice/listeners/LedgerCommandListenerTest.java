package com.maayn.transactionservice.listeners;

import com.maayn.transactionservice.events.TransactionEventPublisher;
import com.maayn.transactionservice.service.TransactionService;
import maayn.veld.generated.errors.TransferException;
import maayn.veld.generated.models.transaction.TransactionResponse;
import maayn.veld.generated.models.transaction.TransactionStatus;
import maayn.veld.generated.models.transaction.TransactionType;
import maayn.veld.generated.models.transaction.TransferRequest;
import maayn.veld.generated.sdk.notification.models.shared.TransferFailedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LedgerCommandListener Unit Tests — SAGA Command RabbitMQ Simulation")
class LedgerCommandListenerTest {

    @Mock private TransactionService transactionService;
    @Mock private TransactionEventPublisher eventPublisher;

    @InjectMocks private LedgerCommandListener listener;

    private TransferRequest command;

    @BeforeEach
    void setUp() {
        command = new TransferRequest();
        command.setIdempotencyKey("saga-cmd-001");
        command.setSourceAccountId(UUID.randomUUID());
        command.setDestinationAccountId(UUID.randomUUID());
        command.setAmount(new BigDecimal("500.00"));
        command.setCurrency("USD");
        command.setType(TransactionType.TRANSFER);
    }

    // ════════════════════════════════════════════════════════════════
    //  handleExecuteTransferCommand() — simulates Card/Financial Service → RabbitMQ → Transaction Service
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When Card/Financial Service sends SAGA TransferRequest via RabbitMQ")
    class SagaTransferCommandTests {

        @Test
        @DisplayName("Should successfully process transfer command from SAGA")
        void handleExecuteTransferCommand_success() throws Exception {
            TransactionResponse response = new TransactionResponse();
            response.setReferenceNumber("TXN-SAGA01");
            response.setAmount(new BigDecimal("500.00"));
            response.setStatus(TransactionStatus.SUCCESS);

            when(transactionService.transfer(command)).thenReturn(response);

            listener.handleExecuteTransferCommand(command);

            verify(transactionService).transfer(command);
            verify(eventPublisher, never()).publish(any(TransferFailedEvent.class));
        }

        @Test
        @DisplayName("Should dispatch failure event when transfer throws insufficient funds")
        void handleExecuteTransferCommand_insufficientFunds_publishesFailure() throws Exception {
            doThrow(TransferException.insufficientFunds("Insufficient funds in account"))
                    .when(transactionService).transfer(command);

            listener.handleExecuteTransferCommand(command);

            ArgumentCaptor<TransferFailedEvent> captor = ArgumentCaptor.forClass(TransferFailedEvent.class);
            verify(eventPublisher).publish(captor.capture());

            TransferFailedEvent failedEvent = captor.getValue();
            assertThat(failedEvent.getReferenceNumber()).isEqualTo("saga-cmd-001");
            assertThat(failedEvent.getSourceAccountId()).isEqualTo(command.getSourceAccountId());
            assertThat(failedEvent.getDestinationAccountId()).isEqualTo(command.getDestinationAccountId());
            assertThat(failedEvent.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(failedEvent.getCurrency()).isEqualTo("USD");
            assertThat(failedEvent.getFailureReason()).contains("Insufficient funds");
        }

        @Test
        @DisplayName("Should dispatch failure event when transfer throws invalid amount")
        void handleExecuteTransferCommand_invalidAmount_publishesFailure() throws Exception {
            doThrow(TransferException.invalidAmount("Transferred amount must be greater than 0"))
                    .when(transactionService).transfer(command);

            listener.handleExecuteTransferCommand(command);

            ArgumentCaptor<TransferFailedEvent> captor = ArgumentCaptor.forClass(TransferFailedEvent.class);
            verify(eventPublisher).publish(captor.capture());

            assertThat(captor.getValue().getFailureReason()).contains("greater than 0");
        }

        @Test
        @DisplayName("Should dispatch failure event when transfer throws invalid target")
        void handleExecuteTransferCommand_invalidTarget_publishesFailure() throws Exception {
            command.setDestinationAccountId(command.getSourceAccountId()); // same account

            doThrow(TransferException.invalidTarget("Source and Destination accounts cannot be the same"))
                    .when(transactionService).transfer(command);

            listener.handleExecuteTransferCommand(command);

            ArgumentCaptor<TransferFailedEvent> captor = ArgumentCaptor.forClass(TransferFailedEvent.class);
            verify(eventPublisher).publish(captor.capture());

            assertThat(captor.getValue().getFailureReason()).contains("cannot be the same");
        }

        @Test
        @DisplayName("Should dispatch failure with generic message for unexpected exceptions")
        void handleExecuteTransferCommand_unexpectedException_publishesFailure() throws Exception {
            doThrow(new RuntimeException("Database connection failed"))
                    .when(transactionService).transfer(command);

            listener.handleExecuteTransferCommand(command);

            ArgumentCaptor<TransferFailedEvent> captor = ArgumentCaptor.forClass(TransferFailedEvent.class);
            verify(eventPublisher).publish(captor.capture());

            assertThat(captor.getValue().getFailureReason()).contains("Database connection failed");
        }

        @Test
        @DisplayName("Should set eventTime on failure event")
        void handleExecuteTransferCommand_failure_setsEventTime() throws Exception {
            doThrow(new RuntimeException("Error"))
                    .when(transactionService).transfer(command);

            listener.handleExecuteTransferCommand(command);

            ArgumentCaptor<TransferFailedEvent> captor = ArgumentCaptor.forClass(TransferFailedEvent.class);
            verify(eventPublisher).publish(captor.capture());

            assertThat(captor.getValue().getEventTime()).isNotNull();
        }

        @Test
        @DisplayName("Should correctly map all command fields to failure event")
        void handleExecuteTransferCommand_failure_mapsAllFields() throws Exception {
            doThrow(new RuntimeException("Test failure"))
                    .when(transactionService).transfer(command);

            listener.handleExecuteTransferCommand(command);

            ArgumentCaptor<TransferFailedEvent> captor = ArgumentCaptor.forClass(TransferFailedEvent.class);
            verify(eventPublisher).publish(captor.capture());

            TransferFailedEvent failedEvent = captor.getValue();
            assertThat(failedEvent.getReferenceNumber()).isEqualTo(command.getIdempotencyKey());
            assertThat(failedEvent.getSourceAccountId()).isEqualTo(command.getSourceAccountId());
            assertThat(failedEvent.getDestinationAccountId()).isEqualTo(command.getDestinationAccountId());
            assertThat(failedEvent.getAmount()).isEqualByComparingTo(command.getAmount());
            assertThat(failedEvent.getCurrency()).isEqualTo(command.getCurrency());
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Simulated Card Service Payment Flow
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Simulated Card Payment SAGA Flow via RabbitMQ")
    class CardPaymentSagaTests {

        @Test
        @DisplayName("Should process card payment transfer command successfully")
        void cardPayment_successfulTransfer() throws Exception {
            TransferRequest cardCommand = new TransferRequest();
            cardCommand.setIdempotencyKey("card-payment-001");
            cardCommand.setSourceAccountId(UUID.randomUUID());
            cardCommand.setDestinationAccountId(UUID.randomUUID());
            cardCommand.setAmount(new BigDecimal("75.99"));
            cardCommand.setCurrency("USD");
            cardCommand.setType(TransactionType.CARD_PAYMENT);

            TransactionResponse response = new TransactionResponse();
            response.setReferenceNumber("TXN-CARD01");
            response.setStatus(TransactionStatus.SUCCESS);

            when(transactionService.transfer(cardCommand)).thenReturn(response);

            listener.handleExecuteTransferCommand(cardCommand);

            verify(transactionService).transfer(cardCommand);
            verify(eventPublisher, never()).publish(any(TransferFailedEvent.class));
        }

        @Test
        @DisplayName("Should publish failure event when card payment has insufficient funds")
        void cardPayment_insufficientFunds_publishesFailure() throws Exception {
            TransferRequest cardCommand = new TransferRequest();
            cardCommand.setIdempotencyKey("card-payment-002");
            cardCommand.setSourceAccountId(UUID.randomUUID());
            cardCommand.setDestinationAccountId(UUID.randomUUID());
            cardCommand.setAmount(new BigDecimal("10000.00"));
            cardCommand.setCurrency("USD");
            cardCommand.setType(TransactionType.CARD_PAYMENT);

            doThrow(TransferException.insufficientFunds("Insufficient funds"))
                    .when(transactionService).transfer(cardCommand);

            listener.handleExecuteTransferCommand(cardCommand);

            verify(eventPublisher).publish(any(TransferFailedEvent.class));
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Simulated Bill Payment Flow
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Simulated Bill Payment Flow via RabbitMQ")
    class BillPaymentTests {

        @Test
        @DisplayName("Should process bill payment transfer command successfully")
        void billPayment_successfulTransfer() throws Exception {
            TransferRequest billCommand = new TransferRequest();
            billCommand.setIdempotencyKey("bill-pay-001");
            billCommand.setSourceAccountId(UUID.randomUUID());
            billCommand.setDestinationAccountId(UUID.randomUUID());
            billCommand.setAmount(new BigDecimal("150.00"));
            billCommand.setCurrency("USD");
            billCommand.setType(TransactionType.BILL_PAYMENT);

            TransactionResponse response = new TransactionResponse();
            response.setReferenceNumber("TXN-BILL01");
            response.setStatus(TransactionStatus.SUCCESS);

            when(transactionService.transfer(billCommand)).thenReturn(response);

            listener.handleExecuteTransferCommand(billCommand);

            verify(transactionService).transfer(billCommand);
        }
    }
}

