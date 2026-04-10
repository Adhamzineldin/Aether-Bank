package com.maayn.transactionservice.mappers;

import com.maayn.transactionservice.entity.Transaction;
import maayn.veld.generated.models.transaction.PaginatedTransactionResponse;
import maayn.veld.generated.models.transaction.TransactionResponse;
import maayn.veld.generated.models.transaction.TransactionStatus;
import maayn.veld.generated.models.transaction.TransactionType;
import maayn.veld.generated.models.transaction.TransferRequest;
import maayn.veld.generated.sdk.notification.models.shared.TransferSuccessEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TransactionMapper Unit Tests")
class TransactionMapperTest {

    // ════════════════════════════════════════════════════════════════
    //  toEntity()
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTests {

        @Test
        @DisplayName("Should map TransferRequest to Transaction entity correctly")
        void toEntity_mapsAllFields() {
            UUID srcId = UUID.randomUUID();
            UUID destId = UUID.randomUUID();

            TransferRequest request = new TransferRequest();
            request.setSourceAccountId(srcId);
            request.setDestinationAccountId(destId);
            request.setAmount(new BigDecimal("250.50"));
            request.setCurrency("EUR");
            request.setType(TransactionType.TRANSFER);
            request.setIdempotencyKey("idem-001");

            Transaction entity = TransactionMapper.toEntity(request);

            assertThat(entity.getSourceAccountId()).isEqualTo(srcId);
            assertThat(entity.getDestinationAccountId()).isEqualTo(destId);
            assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("250.50"));
            assertThat(entity.getCurrency()).isEqualTo("EUR");
            assertThat(entity.getTransactionType()).isEqualTo(TransactionType.TRANSFER);
            assertThat(entity.getStatus()).isEqualTo(TransactionStatus.PENDING);
            assertThat(entity.getIdempotencyKey()).isEqualTo("idem-001");
        }

        @Test
        @DisplayName("Should generate a reference number starting with TXN-")
        void toEntity_generatesReferenceNumber() {
            TransferRequest request = new TransferRequest();
            request.setSourceAccountId(UUID.randomUUID());
            request.setDestinationAccountId(UUID.randomUUID());
            request.setAmount(new BigDecimal("100.00"));
            request.setCurrency("USD");
            request.setType(TransactionType.TRANSFER);
            request.setIdempotencyKey("key");

            Transaction entity = TransactionMapper.toEntity(request);

            assertThat(entity.getReferenceNumber()).startsWith("TXN-");
            assertThat(entity.getReferenceNumber()).hasSize(12); // "TXN-" + 8 chars
        }

        @Test
        @DisplayName("Should always set initial status to PENDING")
        void toEntity_statusIsPending() {
            TransferRequest request = new TransferRequest();
            request.setSourceAccountId(UUID.randomUUID());
            request.setDestinationAccountId(UUID.randomUUID());
            request.setAmount(new BigDecimal("100.00"));
            request.setCurrency("USD");
            request.setType(TransactionType.TRANSFER);
            request.setIdempotencyKey("key");

            Transaction entity = TransactionMapper.toEntity(request);
            assertThat(entity.getStatus()).isEqualTo(TransactionStatus.PENDING);
        }

        @Test
        @DisplayName("Should generate unique reference numbers for multiple calls")
        void toEntity_uniqueReferenceNumbers() {
            TransferRequest request = new TransferRequest();
            request.setSourceAccountId(UUID.randomUUID());
            request.setDestinationAccountId(UUID.randomUUID());
            request.setAmount(new BigDecimal("100.00"));
            request.setCurrency("USD");
            request.setType(TransactionType.TRANSFER);
            request.setIdempotencyKey("key");

            Transaction entity1 = TransactionMapper.toEntity(request);
            Transaction entity2 = TransactionMapper.toEntity(request);

            assertThat(entity1.getReferenceNumber()).isNotEqualTo(entity2.getReferenceNumber());
        }

        @Test
        @DisplayName("Should map all transaction types correctly")
        void toEntity_mapsTransactionTypes() {
            for (TransactionType type : TransactionType.values()) {
                TransferRequest request = new TransferRequest();
                request.setSourceAccountId(UUID.randomUUID());
                request.setDestinationAccountId(UUID.randomUUID());
                request.setAmount(new BigDecimal("100.00"));
                request.setCurrency("USD");
                request.setType(type);
                request.setIdempotencyKey("key");

                Transaction entity = TransactionMapper.toEntity(request);
                assertThat(entity.getTransactionType()).isEqualTo(type);
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  toResponse()
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTests {

        @Test
        @DisplayName("Should map Transaction entity to TransactionResponse")
        void toResponse_mapsAllFields() {
            LocalDateTime now = LocalDateTime.now();
            Transaction txn = Transaction.builder()
                    .referenceNumber("TXN-ABC")
                    .amount(new BigDecimal("123.45"))
                    .status(TransactionStatus.SUCCESS)
                    .createdAt(now)
                    .build();

            TransactionResponse response = TransactionMapper.toResponse(txn);

            assertThat(response.getReferenceNumber()).isEqualTo("TXN-ABC");
            assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("123.45"));
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
            assertThat(response.getTimestamp()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should default timestamp to now when createdAt is null")
        void toResponse_nullCreatedAt_defaultsToNow() {
            Transaction txn = Transaction.builder()
                    .referenceNumber("TXN-ABC")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.PENDING)
                    .createdAt(null)
                    .build();

            TransactionResponse response = TransactionMapper.toResponse(txn);

            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should map FAILED status correctly")
        void toResponse_failedStatus() {
            Transaction txn = Transaction.builder()
                    .referenceNumber("TXN-FAIL")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.FAILED)
                    .createdAt(LocalDateTime.now())
                    .build();

            TransactionResponse response = TransactionMapper.toResponse(txn);
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.FAILED);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  toTransferSuccessEvent()
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toTransferSuccessEvent()")
    class ToTransferSuccessEventTests {

        @Test
        @DisplayName("Should map Transaction to TransferSuccessEvent correctly")
        void toTransferSuccessEvent_mapsAllFields() {
            UUID srcId = UUID.randomUUID();
            UUID destId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            Transaction txn = Transaction.builder()
                    .referenceNumber("TXN-SUCCESS")
                    .sourceAccountId(srcId)
                    .destinationAccountId(destId)
                    .amount(new BigDecimal("300.00"))
                    .currency("GBP")
                    .createdAt(now)
                    .build();

            TransferSuccessEvent event = TransactionMapper.toTransferSuccessEvent(txn);

            assertThat(event.getReferenceNumber()).isEqualTo("TXN-SUCCESS");
            assertThat(event.getSourceAccountId()).isEqualTo(srcId);
            assertThat(event.getDestinationAccountId()).isEqualTo(destId);
            assertThat(event.getAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
            assertThat(event.getCurrency()).isEqualTo("GBP");
            assertThat(event.getEventTime()).isEqualTo(now);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  toPaginatedResponse()
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toPaginatedResponse()")
    class ToPaginatedResponseTests {

        @Test
        @DisplayName("Should map Page<Transaction> to PaginatedTransactionResponse")
        void toPaginatedResponse_mapsCorrectly() {
            Transaction txn = Transaction.builder()
                    .referenceNumber("TXN-P1")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .createdAt(LocalDateTime.now())
                    .build();

            Page<Transaction> page = new PageImpl<>(List.of(txn), PageRequest.of(0, 10), 1);

            PaginatedTransactionResponse response = TransactionMapper.toPaginatedResponse(page);

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getPageNumber()).isEqualTo(0);
            assertThat(response.getPageSize()).isEqualTo(10);
            assertThat(response.getTotalElements()).isEqualTo(1L);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.isIsLast()).isTrue();
        }

        @Test
        @DisplayName("Should handle empty page")
        void toPaginatedResponse_emptyPage() {
            Page<Transaction> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

            PaginatedTransactionResponse response = TransactionMapper.toPaginatedResponse(page);

            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0L);
            assertThat(response.isIsLast()).isTrue();
        }

        @Test
        @DisplayName("Should correctly identify non-last page")
        void toPaginatedResponse_nonLastPage() {
            Transaction txn = Transaction.builder()
                    .referenceNumber("TXN-X")
                    .amount(new BigDecimal("10.00"))
                    .status(TransactionStatus.SUCCESS)
                    .createdAt(LocalDateTime.now())
                    .build();

            Page<Transaction> page = new PageImpl<>(List.of(txn), PageRequest.of(0, 1), 5);

            PaginatedTransactionResponse response = TransactionMapper.toPaginatedResponse(page);

            assertThat(response.isIsLast()).isFalse();
            assertThat(response.getTotalPages()).isEqualTo(5);
        }
    }
}

