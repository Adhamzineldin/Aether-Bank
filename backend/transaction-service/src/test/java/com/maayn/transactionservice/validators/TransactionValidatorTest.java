package com.maayn.transactionservice.validators;

import com.maayn.transactionservice.entity.Transaction;
import maayn.veld.generated.errors.TransferException;
import maayn.veld.generated.models.transaction.TransactionStatus;
import maayn.veld.generated.models.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TransactionValidator Unit Tests")
class TransactionValidatorTest {

    private TransactionValidator validator;
    private UUID sourceId;
    private UUID destId;

    @BeforeEach
    void setUp() {
        validator = new TransactionValidator();
        sourceId = UUID.randomUUID();
        destId = UUID.randomUUID();
    }

    private Transaction buildTransaction(UUID source, UUID dest, BigDecimal amount) {
        return Transaction.builder()
                .sourceAccountId(source)
                .destinationAccountId(dest)
                .amount(amount)
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .referenceNumber("TXN-TEST")
                .currency("USD")
                .idempotencyKey("idem-key")
                .build();
    }

    // ════════════════════════════════════════════════════════════════
    //  validateTransfer()
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Amount Validation")
    class AmountValidationTests {

        @Test
        @DisplayName("Should pass validation for positive amount")
        void validateTransfer_positiveAmount_passes() {
            Transaction txn = buildTransaction(sourceId, destId, new BigDecimal("100.00"));
            assertThatCode(() -> validator.validateTransfer(txn)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw exception for zero amount")
        void validateTransfer_zeroAmount_throws() {
            Transaction txn = buildTransaction(sourceId, destId, BigDecimal.ZERO);

            assertThatThrownBy(() -> validator.validateTransfer(txn))
                    .isInstanceOf(TransferException.class)
                    .hasMessageContaining("greater than 0");
        }

        @Test
        @DisplayName("Should throw exception for negative amount")
        void validateTransfer_negativeAmount_throws() {
            Transaction txn = buildTransaction(sourceId, destId, new BigDecimal("-100.00"));

            assertThatThrownBy(() -> validator.validateTransfer(txn))
                    .isInstanceOf(TransferException.class)
                    .hasMessageContaining("greater than 0");
        }

        @Test
        @DisplayName("Should pass validation for very small positive amount")
        void validateTransfer_verySmallAmount_passes() {
            Transaction txn = buildTransaction(sourceId, destId, new BigDecimal("0.01"));
            assertThatCode(() -> validator.validateTransfer(txn)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass validation for very large positive amount")
        void validateTransfer_veryLargeAmount_passes() {
            Transaction txn = buildTransaction(sourceId, destId, new BigDecimal("999999999999.99"));
            assertThatCode(() -> validator.validateTransfer(txn)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw exception for negative fractional amount")
        void validateTransfer_negativeFraction_throws() {
            Transaction txn = buildTransaction(sourceId, destId, new BigDecimal("-0.01"));

            assertThatThrownBy(() -> validator.validateTransfer(txn))
                    .isInstanceOf(TransferException.class);
        }
    }

    @Nested
    @DisplayName("Account Validation")
    class AccountValidationTests {

        @Test
        @DisplayName("Should pass validation for different source and destination accounts")
        void validateTransfer_differentAccounts_passes() {
            Transaction txn = buildTransaction(sourceId, destId, new BigDecimal("100.00"));
            assertThatCode(() -> validator.validateTransfer(txn)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw exception when source equals destination")
        void validateTransfer_sameAccounts_throws() {
            Transaction txn = buildTransaction(sourceId, sourceId, new BigDecimal("100.00"));

            assertThatThrownBy(() -> validator.validateTransfer(txn))
                    .isInstanceOf(TransferException.class)
                    .hasMessageContaining("cannot be the same");
        }
    }

    @Nested
    @DisplayName("Combined Validation")
    class CombinedValidationTests {

        @Test
        @DisplayName("Should throw for zero amount before checking same account")
        void validateTransfer_zeroAmountSameAccount_throwsAmountError() {
            // Zero amount is checked first
            Transaction txn = buildTransaction(sourceId, sourceId, BigDecimal.ZERO);

            assertThatThrownBy(() -> validator.validateTransfer(txn))
                    .isInstanceOf(TransferException.class)
                    .hasMessageContaining("greater than 0");
        }

        @Test
        @DisplayName("Should throw for same account when amount is valid")
        void validateTransfer_validAmountSameAccount_throwsTargetError() {
            Transaction txn = buildTransaction(sourceId, sourceId, new BigDecimal("100.00"));

            assertThatThrownBy(() -> validator.validateTransfer(txn))
                    .isInstanceOf(TransferException.class)
                    .hasMessageContaining("cannot be the same");
        }
    }
}

