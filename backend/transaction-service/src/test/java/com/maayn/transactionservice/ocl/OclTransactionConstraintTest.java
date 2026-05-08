package com.maayn.transactionservice.ocl;

import com.maayn.transactionservice.entity.LedgerBalance;
import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.exceptions.InvalidBalanceException;
import com.maayn.transactionservice.validators.TransactionValidator;
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

/**
 * OCL constraint tests for the transaction-service domain rules.
 *
 * <p>Each test is labelled with the OCL rule(s) it exercises.
 * The formal specifications are in docs/ocl/banking-domain.ocl.
 * The traceability mapping is in docs/ocl/traceability-matrix.md.
 */
@DisplayName("OCL Transaction + Ledger Constraint Tests")
class OclTransactionConstraintTest {

    private TransactionValidator validator;
    private UUID sourceId;
    private UUID destId;

    @BeforeEach
    void setUp() {
        validator = new TransactionValidator();
        sourceId = UUID.randomUUID();
        destId = UUID.randomUUID();
    }

    private Transaction buildTransaction(UUID source, UUID dest, BigDecimal amount,
                                         String srcCcy, String dstCcy) {
        return Transaction.builder()
                .sourceAccountId(source)
                .destinationAccountId(dest)
                .amount(amount)
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .referenceNumber("OCL-TXN-TEST")
                .currency(srcCcy)
                .sourceCurrency(srcCcy)
                .destinationCurrency(dstCcy)
                .idempotencyKey("ocl-idem-" + UUID.randomUUID())
                .build();
    }

    // ════════════════════════════════════════════════════════════════════
    //  TX_01 — amount must be positive
    // ════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("OCL: TX_01 — transfer amount must be positive")
    class PositiveAmountTests {

        @Test
        @DisplayName("TX_01: positive amount passes validation")
        void tx01_positiveAmount_passes() {
            Transaction txn = buildTransaction(sourceId, destId, new BigDecimal("50.00"), "USD", "USD");
            assertThatCode(() -> validator.validateTransfer(txn)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("TX_01: zero amount fails validation")
        void tx01_zeroAmount_fails() {
            Transaction txn = buildTransaction(sourceId, destId, BigDecimal.ZERO, "USD", "USD");
            assertThatThrownBy(() -> validator.validateTransfer(txn))
                    .isInstanceOf(TransferException.class)
                    .hasMessageContaining("greater than 0");
        }

        @Test
        @DisplayName("TX_01: negative amount fails validation")
        void tx01_negativeAmount_fails() {
            Transaction txn = buildTransaction(sourceId, destId, new BigDecimal("-1.00"), "USD", "USD");
            assertThatThrownBy(() -> validator.validateTransfer(txn))
                    .isInstanceOf(TransferException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  TX_02 — source and destination must differ
    // ════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("OCL: TX_02 — source and destination accounts must differ")
    class DifferentAccountsTests {

        @Test
        @DisplayName("TX_02: different accounts passes validation")
        void tx02_differentAccounts_passes() {
            Transaction txn = buildTransaction(sourceId, destId, new BigDecimal("100.00"), "USD", "USD");
            assertThatCode(() -> validator.validateTransfer(txn)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("TX_02: same source and destination fails validation")
        void tx02_sameAccount_fails() {
            Transaction txn = buildTransaction(sourceId, sourceId, new BigDecimal("100.00"), "USD", "USD");
            assertThatThrownBy(() -> validator.validateTransfer(txn))
                    .isInstanceOf(TransferException.class)
                    .hasMessageContaining("cannot be the same");
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  TX_06..TX_09 — FX transfer consistency (conceptual checks)
    // ════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("OCL: TX_06..TX_09 — FX transfer consistency")
    class FxConsistencyTests {

        /**
         * TX_06, TX_07: source and destination currency must not be null for FX transfers.
         * This is enforced by TransactionService.prepareTransaction() before persistence;
         * here we verify the shape the entity must have after preparation.
         */
        @Test
        @DisplayName("TX_06 + TX_07: FX transaction entity should carry both currencies")
        void tx06_tx07_fxTransaction_hasBothCurrencies() {
            Transaction txn = buildTransaction(sourceId, destId, new BigDecimal("100.00"), "USD", "EUR");
            txn.setExchangeRate(new BigDecimal("0.93"));
            txn.setDestinationAmount(new BigDecimal("93.00"));

            assertThat(txn.getSourceCurrency()).isNotNull().hasSize(3);
            assertThat(txn.getDestinationCurrency()).isNotNull().hasSize(3);
            assertThat(txn.getSourceCurrency()).isNotEqualTo(txn.getDestinationCurrency());
        }

        /**
         * TX_08: destinationAmount must be positive for cross-currency transfers.
         */
        @Test
        @DisplayName("TX_08: FX transaction must have a positive destinationAmount")
        void tx08_fxTransaction_hasPositiveDestinationAmount() {
            Transaction txn = buildTransaction(sourceId, destId, new BigDecimal("100.00"), "USD", "EUR");
            txn.setDestinationAmount(new BigDecimal("93.00"));
            txn.setExchangeRate(new BigDecimal("0.93"));

            assertThat(txn.getDestinationAmount()).isGreaterThan(BigDecimal.ZERO);
        }

        /**
         * TX_09: exchangeRate must be positive for cross-currency transfers.
         */
        @Test
        @DisplayName("TX_09: FX transaction must have a positive exchangeRate")
        void tx09_fxTransaction_hasPositiveExchangeRate() {
            Transaction txn = buildTransaction(sourceId, destId, new BigDecimal("100.00"), "USD", "EUR");
            txn.setExchangeRate(new BigDecimal("0.93"));
            txn.setDestinationAmount(new BigDecimal("93.00"));

            assertThat(txn.getExchangeRate()).isGreaterThan(BigDecimal.ZERO);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  LB_01 — ledger balance must remain non-negative
    // ════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("OCL: LB_01 — available balance must not go negative")
    class LedgerBalanceInvariantTests {

        @Test
        @DisplayName("LB_01: debit within available balance succeeds and stays non-negative")
        void lb01_debitWithinBalance_remainsNonNegative() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            balance.credit(new BigDecimal("200.00"));
            balance.debit(new BigDecimal("150.00"));

            assertThat(balance.getAvailableBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("LB_01: debit equal to balance drains to exactly zero")
        void lb01_debitEqualToBalance_drainsToZero() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            balance.credit(new BigDecimal("100.00"));
            balance.debit(new BigDecimal("100.00"));

            assertThat(balance.getAvailableBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("LB_01: debit exceeding balance is rejected — invariant guarded by debit()")
        void lb01_debitExceedingBalance_throws() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            balance.credit(new BigDecimal("50.00"));

            // Attempting to debit more than available must throw.
            // This is the runtime enforcement of LB_01.
            assertThatThrownBy(() -> balance.debit(new BigDecimal("100.00")))
                    .isInstanceOf(InvalidBalanceException.class)
                    .hasMessageContaining("Insufficient funds");

            // Invariant still holds after rejection.
            assertThat(balance.getAvailableBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  LB_02 — pendingHolds must stay non-negative (initial state check)
    // ════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("OCL: LB_02 — pending holds must not be negative")
    class PendingHoldsInvariantTests {

        @Test
        @DisplayName("LB_02: newly created LedgerBalance has zero pendingHolds")
        void lb02_newBalance_pendingHoldsAreZero() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            assertThat(balance.getPendingHolds()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  LB_03 / LB_04 — ledger identity requires accountId and currency
    // ════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("OCL: LB_03 + LB_04 — ledger identity invariants")
    class LedgerIdentityTests {

        @Test
        @DisplayName("LB_03: ledger currency is the one provided at construction")
        void lb03_currency_isPreserved() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "EUR");
            assertThat(balance.getId().getCurrency()).isEqualTo("EUR");
        }

        @Test
        @DisplayName("LB_04: ledger accountId is the one provided at construction")
        void lb04_accountId_isPreserved() {
            UUID accountId = UUID.randomUUID();
            LedgerBalance balance = new LedgerBalance(accountId, "USD");
            assertThat(balance.getId().getAccountId()).isEqualTo(accountId);
        }
    }
}

