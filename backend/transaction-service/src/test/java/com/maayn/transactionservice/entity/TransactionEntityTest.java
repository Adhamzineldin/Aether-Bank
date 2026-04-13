package com.maayn.transactionservice.entity;

import maayn.veld.generated.models.transaction.TransactionStatus;
import maayn.veld.generated.models.transaction.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Transaction Entity Unit Tests")
class TransactionEntityTest {

    // ════════════════════════════════════════════════════════════════
    //  Builder / Basic Properties
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("Should build a complete Transaction entity")
        void builder_createsTransaction() {
            UUID srcId = UUID.randomUUID();
            UUID destId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            Transaction txn = Transaction.builder()
                    .id(UUID.randomUUID())
                    .sourceAccountId(srcId)
                    .destinationAccountId(destId)
                    .amount(new BigDecimal("250.00"))
                    .transactionType(TransactionType.TRANSFER)
                    .status(TransactionStatus.PENDING)
                    .idempotencyKey("idem-123")
                    .referenceNumber("TXN-TEST01")
                    .currency("USD")
                    .createdAt(now)
                    .updatedAt(now)
                    .version(0L)
                    .build();

            assertThat(txn.getSourceAccountId()).isEqualTo(srcId);
            assertThat(txn.getDestinationAccountId()).isEqualTo(destId);
            assertThat(txn.getAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
            assertThat(txn.getTransactionType()).isEqualTo(TransactionType.TRANSFER);
            assertThat(txn.getStatus()).isEqualTo(TransactionStatus.PENDING);
            assertThat(txn.getIdempotencyKey()).isEqualTo("idem-123");
            assertThat(txn.getReferenceNumber()).isEqualTo("TXN-TEST01");
            assertThat(txn.getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Should allow null failure reason for non-failed transactions")
        void builder_nullFailureReason() {
            Transaction txn = Transaction.builder()
                    .status(TransactionStatus.SUCCESS)
                    .failureReason(null)
                    .build();

            assertThat(txn.getFailureReason()).isNull();
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  applySagaResult()
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("applySagaResult()")
    class ApplySagaResultTests {

        @Test
        @DisplayName("Should set status to SUCCESS and leave failure reason null")
        void applySagaResult_success() {
            Transaction txn = Transaction.builder()
                    .status(TransactionStatus.PENDING)
                    .build();

            txn.applySagaResult(TransactionStatus.SUCCESS, "Transaction completed");

            assertThat(txn.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
            assertThat(txn.getFailureReason()).isNull();
        }

        @Test
        @DisplayName("Should set status to FAILED and populate failure reason")
        void applySagaResult_failed() {
            Transaction txn = Transaction.builder()
                    .status(TransactionStatus.PENDING)
                    .build();

            txn.applySagaResult(TransactionStatus.FAILED, "Insufficient funds");

            assertThat(txn.getStatus()).isEqualTo(TransactionStatus.FAILED);
            assertThat(txn.getFailureReason()).isEqualTo("Insufficient funds");
        }

        @Test
        @DisplayName("Should overwrite existing status")
        void applySagaResult_overwritesStatus() {
            Transaction txn = Transaction.builder()
                    .status(TransactionStatus.PENDING)
                    .build();

            txn.applySagaResult(TransactionStatus.COMPLETED, null);

            assertThat(txn.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should set failure reason only when status is FAILED")
        void applySagaResult_onlyFailedSetsReason() {
            Transaction txn = Transaction.builder()
                    .status(TransactionStatus.PENDING)
                    .build();

            txn.applySagaResult(TransactionStatus.COMPLETED, "Some reason");
            assertThat(txn.getFailureReason()).isNull(); // Not FAILED, so reason not set

            txn.applySagaResult(TransactionStatus.FAILED, "Real failure");
            assertThat(txn.getFailureReason()).isEqualTo("Real failure");
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  ensureCreatedAt() (@PrePersist)
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ensureCreatedAt()")
    class EnsureCreatedAtTests {

        @Test
        @DisplayName("Should build with explicit createdAt")
        void explicitCreatedAt() {
            LocalDateTime explicit = LocalDateTime.of(2025, 1, 1, 12, 0);
            Transaction txn = Transaction.builder()
                    .createdAt(explicit)
                    .build();

            assertThat(txn.getCreatedAt()).isEqualTo(explicit);
        }

        @Test
        @DisplayName("Should allow null createdAt from builder (JPA will handle @PrePersist)")
        void nullCreatedAt_fromBuilder() {
            Transaction txn = Transaction.builder()
                    .createdAt(null)
                    .build();

            assertThat(txn.getCreatedAt()).isNull();
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  All TransactionType values
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TransactionType Coverage")
    class TransactionTypeTests {

        @Test
        @DisplayName("Should support TRANSFER type")
        void transferType() {
            Transaction txn = Transaction.builder().transactionType(TransactionType.TRANSFER).build();
            assertThat(txn.getTransactionType()).isEqualTo(TransactionType.TRANSFER);
        }

        @Test
        @DisplayName("Should support INTERNAL_TRANSFER type")
        void internalTransferType() {
            Transaction txn = Transaction.builder().transactionType(TransactionType.INTERNAL_TRANSFER).build();
            assertThat(txn.getTransactionType()).isEqualTo(TransactionType.INTERNAL_TRANSFER);
        }

        @Test
        @DisplayName("Should support DEPOSIT type")
        void depositType() {
            Transaction txn = Transaction.builder().transactionType(TransactionType.DEPOSIT).build();
            assertThat(txn.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        }

        @Test
        @DisplayName("Should support WITHDRAWAL type")
        void withdrawalType() {
            Transaction txn = Transaction.builder().transactionType(TransactionType.WITHDRAWAL).build();
            assertThat(txn.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        }

        @Test
        @DisplayName("Should support CARD_PAYMENT type")
        void cardPaymentType() {
            Transaction txn = Transaction.builder().transactionType(TransactionType.CARD_PAYMENT).build();
            assertThat(txn.getTransactionType()).isEqualTo(TransactionType.CARD_PAYMENT);
        }

        @Test
        @DisplayName("Should support BILL_PAYMENT type")
        void billPaymentType() {
            Transaction txn = Transaction.builder().transactionType(TransactionType.BILL_PAYMENT).build();
            assertThat(txn.getTransactionType()).isEqualTo(TransactionType.BILL_PAYMENT);
        }
    }
}

