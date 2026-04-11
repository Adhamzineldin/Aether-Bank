package com.maayn.transactionservice.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LedgerBalance Entity Unit Tests")
class LedgerBalanceTest {

    // ════════════════════════════════════════════════════════════════
    //  Constructor
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize with given accountId and currency")
        void constructor_setsAccountId() {
            UUID id = UUID.randomUUID();
            LedgerBalance balance = new LedgerBalance(id, "USD");
            assertThat(balance.getId().getAccountId()).isEqualTo(id);
            assertThat(balance.getId().getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Should initialize available balance to zero")
        void constructor_zeroAvailableBalance() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            assertThat(balance.getAvailableBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should initialize pending holds to zero")
        void constructor_zeroPendingHolds() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            assertThat(balance.getPendingHolds()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  debit()
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("debit()")
    class DebitTests {

        @Test
        @DisplayName("Should subtract amount from available balance")
        void debit_subtractsAmount() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            balance.setAvailableBalance(new BigDecimal("500.00"));

            balance.debit(new BigDecimal("200.00"));

            assertThat(balance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("300.00"));
        }

        @Test
        @DisplayName("Should allow debit of exact available balance (drain to zero)")
        void debit_exactAmount_drainsToZero() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            balance.setAvailableBalance(new BigDecimal("100.00"));

            balance.debit(new BigDecimal("100.00"));

            assertThat(balance.getAvailableBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should throw exception when amount exceeds balance")
        void debit_exceedsBalance_throwsException() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            balance.setAvailableBalance(new BigDecimal("50.00"));

            assertThatThrownBy(() -> balance.debit(new BigDecimal("100.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient funds");
        }

        @Test
        @DisplayName("Should throw when debiting from zero balance")
        void debit_zeroBalance_throwsException() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");

            assertThatThrownBy(() -> balance.debit(new BigDecimal("1.00")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should handle debit of very small amount")
        void debit_verySmallAmount() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            balance.setAvailableBalance(new BigDecimal("1.00"));

            balance.debit(new BigDecimal("0.01"));

            assertThat(balance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("0.99"));
        }

        @Test
        @DisplayName("Should handle debit of very large amount from sufficient balance")
        void debit_largeAmount_success() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            balance.setAvailableBalance(new BigDecimal("999999999999.99"));

            balance.debit(new BigDecimal("999999999999.98"));

            assertThat(balance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("0.01"));
        }

        @Test
        @DisplayName("Should fail when debit exceeds balance by 0.01")
        void debit_exceedsByOneCent_throwsException() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            balance.setAvailableBalance(new BigDecimal("99.99"));

            assertThatThrownBy(() -> balance.debit(new BigDecimal("100.00")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  credit()
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("credit()")
    class CreditTests {

        @Test
        @DisplayName("Should add amount to available balance")
        void credit_addsAmount() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            balance.setAvailableBalance(new BigDecimal("200.00"));

            balance.credit(new BigDecimal("300.00"));

            assertThat(balance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("Should credit from zero balance")
        void credit_fromZero() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");

            balance.credit(new BigDecimal("1000.00"));

            assertThat(balance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Should handle credit of very small amount")
        void credit_verySmallAmount() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            balance.setAvailableBalance(new BigDecimal("0.00"));

            balance.credit(new BigDecimal("0.01"));

            assertThat(balance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("0.01"));
        }

        @Test
        @DisplayName("Should accumulate multiple credits")
        void credit_multipleCredits() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");

            balance.credit(new BigDecimal("100.00"));
            balance.credit(new BigDecimal("200.00"));
            balance.credit(new BigDecimal("300.00"));

            assertThat(balance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("600.00"));
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Combined debit + credit
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Combined Operations")
    class CombinedTests {

        @Test
        @DisplayName("Should handle credit then debit correctly")
        void creditThenDebit() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            balance.credit(new BigDecimal("500.00"));
            balance.debit(new BigDecimal("200.00"));

            assertThat(balance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("300.00"));
        }

        @Test
        @DisplayName("Should handle sequence of credits and debits")
        void multipleOperations() {
            LedgerBalance balance = new LedgerBalance(UUID.randomUUID(), "USD");
            balance.credit(new BigDecimal("1000.00"));
            balance.debit(new BigDecimal("250.00"));
            balance.credit(new BigDecimal("100.00"));
            balance.debit(new BigDecimal("50.00"));

            assertThat(balance.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("800.00"));
        }
    }
}

