package com.maayn.transactionservice.service;

import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.events.TransactionEventPublisher;
import com.maayn.transactionservice.handlers.TransferIdempotencyHandler;
import com.maayn.transactionservice.repository.TransactionRepository;
import com.maayn.transactionservice.validators.TransactionValidator;
import maayn.veld.generated.errors.TransferException;
import maayn.veld.generated.models.transaction.PaginatedTransactionResponse;
import maayn.veld.generated.models.transaction.TransactionResponse;
import maayn.veld.generated.models.transaction.TransactionStatus;
import maayn.veld.generated.models.transaction.TransactionType;
import maayn.veld.generated.models.transaction.TransferRequest;
import maayn.veld.generated.models.transaction.getAccountTransactionsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private LedgerService ledgerService;
    @Mock private TransactionEventPublisher eventPublisher;
    @Mock private TransactionValidator validator;
    @Mock private TransferIdempotencyHandler idempotencyHandler;

    @InjectMocks private TransactionService transactionService;

    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private TransferRequest validRequest;

    @BeforeEach
    void setUp() {
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();

        validRequest = new TransferRequest();
        validRequest.setIdempotencyKey("idem-key-001");
        validRequest.setSourceAccountId(sourceAccountId);
        validRequest.setDestinationAccountId(destinationAccountId);
        validRequest.setAmount(new BigDecimal("100.00"));
        validRequest.setCurrency("USD");
        validRequest.setType(TransactionType.TRANSFER);
    }

    // ════════════════════════════════════════════════════════════════
    //  transfer()
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("transfer()")
    class TransferTests {

        @Test
        @DisplayName("Should successfully create a new transfer transaction")
        void transfer_success() throws Exception {
            when(idempotencyHandler.getIfAlreadyProcessed(anyString()))
                    .thenReturn(Optional.empty());

            Transaction saved = buildSavedTransaction(TransactionStatus.SUCCESS);
            when(transactionRepository.saveAndFlush(any(Transaction.class))).thenReturn(saved);

            TransactionResponse response = transactionService.transfer(validRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
            assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));

            verify(validator).validateTransfer(any(Transaction.class));
            verify(ledgerService).executeTransferMath(eq(sourceAccountId), eq(destinationAccountId), eq(new BigDecimal("100.00")));
            verify(transactionRepository).saveAndFlush(any(Transaction.class));
            verify(eventPublisher).publish(any(maayn.veld.generated.sdk.notification.models.shared.TransferSuccessEvent.class));
        }

        @Test
        @DisplayName("Should return cached response for idempotent duplicate request")
        void transfer_idempotentDuplicate_returnsCachedResponse() throws Exception {
            TransactionResponse cachedResponse = new TransactionResponse();
            cachedResponse.setReferenceNumber("TXN-EXISTING");
            cachedResponse.setAmount(new BigDecimal("100.00"));
            cachedResponse.setStatus(TransactionStatus.SUCCESS);

            when(idempotencyHandler.getIfAlreadyProcessed("idem-key-001"))
                    .thenReturn(Optional.of(cachedResponse));

            TransactionResponse response = transactionService.transfer(validRequest);

            assertThat(response.getReferenceNumber()).isEqualTo("TXN-EXISTING");
            verify(transactionRepository, never()).saveAndFlush(any());
            verify(ledgerService, never()).executeTransferMath(any(), any(), any());
            verify(eventPublisher, never()).publish(any(maayn.veld.generated.sdk.notification.models.shared.TransferSuccessEvent.class));
        }

        @Test
        @DisplayName("Should propagate validation exception for invalid amount")
        void transfer_invalidAmount_throwsException() {
            when(idempotencyHandler.getIfAlreadyProcessed(anyString()))
                    .thenReturn(Optional.empty());

            validRequest.setAmount(BigDecimal.ZERO);

            doThrow(TransferException.invalidAmount("Transferred amount must be greater than 0"))
                    .when(validator).validateTransfer(any(Transaction.class));

            assertThatThrownBy(() -> transactionService.transfer(validRequest))
                    .isInstanceOf(TransferException.class)
                    .hasMessageContaining("greater than 0");

            verify(transactionRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should propagate validation exception for same source and destination")
        void transfer_sameAccount_throwsException() {
            when(idempotencyHandler.getIfAlreadyProcessed(anyString()))
                    .thenReturn(Optional.empty());

            validRequest.setDestinationAccountId(sourceAccountId);

            doThrow(TransferException.invalidTarget("Source and Destination accounts cannot be the same"))
                    .when(validator).validateTransfer(any(Transaction.class));

            assertThatThrownBy(() -> transactionService.transfer(validRequest))
                    .isInstanceOf(TransferException.class)
                    .hasMessageContaining("cannot be the same");
        }

        @Test
        @DisplayName("Should propagate insufficient funds exception from ledger")
        void transfer_insufficientFunds_throwsException() {
            when(idempotencyHandler.getIfAlreadyProcessed(anyString()))
                    .thenReturn(Optional.empty());

            doThrow(TransferException.insufficientFunds("Insufficient funds in account"))
                    .when(ledgerService).executeTransferMath(any(), any(), any());

            assertThatThrownBy(() -> transactionService.transfer(validRequest))
                    .isInstanceOf(TransferException.class)
                    .hasMessageContaining("Insufficient funds");

            verify(transactionRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should set transaction status to SUCCESS before persisting")
        void transfer_setsStatusToSuccess() throws Exception {
            when(idempotencyHandler.getIfAlreadyProcessed(anyString()))
                    .thenReturn(Optional.empty());

            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            Transaction saved = buildSavedTransaction(TransactionStatus.SUCCESS);
            when(transactionRepository.saveAndFlush(captor.capture())).thenReturn(saved);

            transactionService.transfer(validRequest);

            Transaction captured = captor.getValue();
            assertThat(captured.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        }

        @Test
        @DisplayName("Should set idempotency key on transaction from request")
        void transfer_setsIdempotencyKey() throws Exception {
            when(idempotencyHandler.getIfAlreadyProcessed(anyString()))
                    .thenReturn(Optional.empty());

            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            Transaction saved = buildSavedTransaction(TransactionStatus.SUCCESS);
            when(transactionRepository.saveAndFlush(captor.capture())).thenReturn(saved);

            transactionService.transfer(validRequest);

            assertThat(captor.getValue().getIdempotencyKey()).isEqualTo("idem-key-001");
        }

        @Test
        @DisplayName("Should publish success event after persisting transaction")
        void transfer_publishesSuccessEvent() throws Exception {
            when(idempotencyHandler.getIfAlreadyProcessed(anyString()))
                    .thenReturn(Optional.empty());

            Transaction saved = buildSavedTransaction(TransactionStatus.SUCCESS);
            when(transactionRepository.saveAndFlush(any())).thenReturn(saved);

            transactionService.transfer(validRequest);

            verify(eventPublisher).publish(any(maayn.veld.generated.sdk.notification.models.shared.TransferSuccessEvent.class));
        }

        @Test
        @DisplayName("Should handle negative amount via validator")
        void transfer_negativeAmount_throwsException() {
            when(idempotencyHandler.getIfAlreadyProcessed(anyString()))
                    .thenReturn(Optional.empty());

            validRequest.setAmount(new BigDecimal("-50.00"));

            doThrow(TransferException.invalidAmount("Transferred amount must be greater than 0"))
                    .when(validator).validateTransfer(any(Transaction.class));

            assertThatThrownBy(() -> transactionService.transfer(validRequest))
                    .isInstanceOf(TransferException.class);
        }

        @Test
        @DisplayName("Should handle very large transfer amounts")
        void transfer_veryLargeAmount_success() throws Exception {
            when(idempotencyHandler.getIfAlreadyProcessed(anyString()))
                    .thenReturn(Optional.empty());

            validRequest.setAmount(new BigDecimal("999999999999.99"));

            Transaction saved = buildSavedTransaction(TransactionStatus.SUCCESS);
            saved.setAmount(new BigDecimal("999999999999.99"));
            when(transactionRepository.saveAndFlush(any())).thenReturn(saved);

            TransactionResponse response = transactionService.transfer(validRequest);

            assertThat(response).isNotNull();
            assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("999999999999.99"));
        }

        @Test
        @DisplayName("Should handle very small fractional amounts")
        void transfer_smallFractionalAmount_success() throws Exception {
            when(idempotencyHandler.getIfAlreadyProcessed(anyString()))
                    .thenReturn(Optional.empty());

            validRequest.setAmount(new BigDecimal("0.01"));

            Transaction saved = buildSavedTransaction(TransactionStatus.SUCCESS);
            saved.setAmount(new BigDecimal("0.01"));
            when(transactionRepository.saveAndFlush(any())).thenReturn(saved);

            TransactionResponse response = transactionService.transfer(validRequest);

            assertThat(response).isNotNull();
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  getAccountTransactions()
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAccountTransactions()")
    class GetAccountTransactionsTests {

        @Test
        @DisplayName("Should return paginated transactions for a valid account")
        void getAccountTransactions_success() throws Exception {
            Transaction txn = buildSavedTransaction(TransactionStatus.SUCCESS);
            Page<Transaction> page = new PageImpl<>(List.of(txn), PageRequest.of(0, 10), 1);

            when(transactionRepository
                    .findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(any(UUID.class), any(UUID.class), any(Pageable.class)))
                    .thenReturn(page);

            getAccountTransactionsRequest input = new getAccountTransactionsRequest();
            input.setPage(0);
            input.setPageSize(10);

            PaginatedTransactionResponse result = transactionService.getAccountTransactions(sourceAccountId.toString(), input);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);
            assertThat(result.getPageNumber()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return empty page when no transactions exist")
        void getAccountTransactions_emptyResult() throws Exception {
            Page<Transaction> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

            when(transactionRepository
                    .findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(any(UUID.class), any(UUID.class), any(Pageable.class)))
                    .thenReturn(emptyPage);

            getAccountTransactionsRequest input = new getAccountTransactionsRequest();
            input.setPage(0);
            input.setPageSize(10);

            PaginatedTransactionResponse result = transactionService.getAccountTransactions(sourceAccountId.toString(), input);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should return multiple transactions across pages")
        void getAccountTransactions_multiplePagesMetadata() throws Exception {
            Transaction txn1 = buildSavedTransaction(TransactionStatus.SUCCESS);
            Transaction txn2 = buildSavedTransaction(TransactionStatus.SUCCESS);
            txn2.setReferenceNumber("TXN-SECOND");

            Page<Transaction> page = new PageImpl<>(List.of(txn1, txn2), PageRequest.of(0, 2), 5);

            when(transactionRepository
                    .findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(any(UUID.class), any(UUID.class), any(Pageable.class)))
                    .thenReturn(page);

            getAccountTransactionsRequest input = new getAccountTransactionsRequest();
            input.setPage(0);
            input.setPageSize(2);

            PaginatedTransactionResponse result = transactionService.getAccountTransactions(sourceAccountId.toString(), input);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(5L);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.getIsLast()).isFalse();
        }

        @Test
        @DisplayName("Should correctly identify last page")
        void getAccountTransactions_lastPage() throws Exception {
            Transaction txn = buildSavedTransaction(TransactionStatus.SUCCESS);
            Page<Transaction> page = new PageImpl<>(List.of(txn), PageRequest.of(2, 2), 5);

            when(transactionRepository
                    .findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(any(UUID.class), any(UUID.class), any(Pageable.class)))
                    .thenReturn(page);

            getAccountTransactionsRequest input = new getAccountTransactionsRequest();
            input.setPage(2);
            input.setPageSize(2);

            PaginatedTransactionResponse result = transactionService.getAccountTransactions(sourceAccountId.toString(), input);

            assertThat(result.getIsLast()).isTrue();
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════

    private Transaction buildSavedTransaction(TransactionStatus status) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .amount(new BigDecimal("100.00"))
                .transactionType(TransactionType.TRANSFER)
                .status(status)
                .idempotencyKey("idem-key-001")
                .referenceNumber("TXN-ABCDEF12")
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(0L)
                .build();
    }
}

