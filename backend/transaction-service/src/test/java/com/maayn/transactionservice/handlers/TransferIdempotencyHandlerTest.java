package com.maayn.transactionservice.handlers;

import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.repository.TransactionRepository;
import maayn.veld.generated.models.transaction.TransactionResponse;
import maayn.veld.generated.models.transaction.TransactionStatus;
import maayn.veld.generated.models.transaction.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferIdempotencyHandler Unit Tests")
class TransferIdempotencyHandlerTest {

    @Mock private TransactionRepository repository;

    @InjectMocks private TransferIdempotencyHandler handler;

    @Test
    @DisplayName("Should return existing response when transaction already processed")
    void getIfAlreadyProcessed_exists_returnsResponse() {
        Transaction existing = Transaction.builder()
                .referenceNumber("TXN-EXISTING")
                .amount(new BigDecimal("200.00"))
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findByIdempotencyKey("idem-001")).thenReturn(Optional.of(existing));

        Optional<TransactionResponse> result = handler.getIfAlreadyProcessed("idem-001");

        assertThat(result).isPresent();
        assertThat(result.get().getReferenceNumber()).isEqualTo("TXN-EXISTING");
        assertThat(result.get().getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(result.get().getStatus()).isEqualTo(TransactionStatus.SUCCESS);
    }

    @Test
    @DisplayName("Should return empty when transaction not found")
    void getIfAlreadyProcessed_notFound_returnsEmpty() {
        when(repository.findByIdempotencyKey("idem-new")).thenReturn(Optional.empty());

        Optional<TransactionResponse> result = handler.getIfAlreadyProcessed("idem-new");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return existing response for FAILED transaction too")
    void getIfAlreadyProcessed_failedTransaction_returnsResponse() {
        Transaction failed = Transaction.builder()
                .referenceNumber("TXN-FAILED")
                .amount(new BigDecimal("100.00"))
                .status(TransactionStatus.FAILED)
                .createdAt(LocalDateTime.now())
                .failureReason("Insufficient funds")
                .build();

        when(repository.findByIdempotencyKey("idem-fail")).thenReturn(Optional.of(failed));

        Optional<TransactionResponse> result = handler.getIfAlreadyProcessed("idem-fail");

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TransactionStatus.FAILED);
    }

    @Test
    @DisplayName("Should return existing response for PENDING transaction")
    void getIfAlreadyProcessed_pendingTransaction_returnsResponse() {
        Transaction pending = Transaction.builder()
                .referenceNumber("TXN-PENDING")
                .amount(new BigDecimal("100.00"))
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findByIdempotencyKey("idem-pend")).thenReturn(Optional.of(pending));

        Optional<TransactionResponse> result = handler.getIfAlreadyProcessed("idem-pend");

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    @DisplayName("Should call repository exactly once per invocation")
    void getIfAlreadyProcessed_callsRepositoryOnce() {
        when(repository.findByIdempotencyKey("key")).thenReturn(Optional.empty());

        handler.getIfAlreadyProcessed("key");

        verify(repository, times(1)).findByIdempotencyKey("key");
    }
}

