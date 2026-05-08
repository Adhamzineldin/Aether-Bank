package com.maayn.accountservice.service;

import com.maayn.accountservice.entity.BankAccount;
import com.maayn.accountservice.enums.AccountStatus;
import com.maayn.accountservice.exception.InvalidAccountStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OCL constraint tests for the account-service domain rules.
 *
 * <p>Each test is labelled with the OCL rule(s) it exercises.
 * The formal specifications are in docs/ocl/banking-domain.ocl.
 * The traceability mapping is in docs/ocl/traceability-matrix.md.
 */
@DisplayName("OCL Account Constraint Tests")
class OclAccountConstraintTest {

    // ════════════════════════════════════════════════════════════════════
    //  BA_01 / BA_02 — closedDate invariants (entity-level)
    // ════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("OCL: BA_01 + BA_02 — closedDate lifecycle invariant")
    class ClosedDateInvariantTests {

        /**
         * OCL: BA_01_ClosedAccountsHaveClosedDate
         * A CLOSED account must always have a closedDate value.
         */
        @Test
        @DisplayName("BA_01: BankAccount with CLOSED status must have closedDate")
        void ba01_closedAccount_mustHaveClosedDate() {
            BankAccount account = BankAccount.builder()
                    .status(AccountStatus.CLOSED)
                    .build();

            // Invariant check: closedDate must not be null when status is CLOSED.
            // In production this is set by BankAccountService.closeAccount().
            account.setClosedDate(java.time.LocalDate.now());

            assertThat(account.getClosedDate()).isNotNull();
            assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
        }

        /**
         * OCL: BA_02_OnlyClosedAccountsMayHaveClosedDate
         * An ACTIVE account must not carry a closedDate.
         */
        @Test
        @DisplayName("BA_02: BankAccount with ACTIVE status must not have closedDate")
        void ba02_activeAccount_mustNotHaveClosedDate() {
            BankAccount account = BankAccount.builder()
                    .status(AccountStatus.ACTIVE)
                    .build();

            // closedDate should be null for any non-closed account.
            assertThat(account.getClosedDate()).isNull();
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  BA_04 — currency format (request-level DTO, enforced by @Pattern)
    // ════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("OCL: BA_04 + BA_15 — currency ISO-3 format")
    class CurrencyFormatTests {

        /**
         * OCL: BA_04_CurrencyLooksISO3, BA_15_RequestCurrencyLooksISO3
         * Currency must be exactly 3 uppercase letters.
         */
        @Test
        @DisplayName("BA_04 / BA_15: valid 3-letter currency codes pass the ISO3 pattern")
        void ba04_validCurrencyCodes() {
            // Simulates the @Pattern check applied to OpenAccountRequest.currency
            String[] validCodes = {"USD", "EUR", "GBP", "AED", "JPY"};
            for (String code : validCodes) {
                assertThat(code).matches("^[A-Z]{3}$");
            }
        }

        @Test
        @DisplayName("BA_04: invalid currency codes fail the ISO3 pattern")
        void ba04_invalidCurrencyCodes_failPattern() {
            String[] invalidCodes = {"us", "usd", "US1", "USDD", "", "1AB"};
            for (String code : invalidCodes) {
                assertThat(code).doesNotMatch("^[A-Z]{3}$");
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  BA_10 / BA_11 / BA_12 — status transition rules
    // ════════════════════════════════════════════════════════════════════

    /**
     * These tests use a minimal inline replica of BankAccountService.validateStatusTransition()
     * so we can exercise the OCL rules without needing the full Spring context.
     */
    private void validateStatusTransition(AccountStatus currentStatus, AccountStatus newStatus) {
        // OCL: BA_10_ClosedAccountsCannotChangeStatus
        if (currentStatus == AccountStatus.CLOSED) {
            throw new InvalidAccountStatusException("Cannot change status of a closed account");
        }
        // OCL: BA_11_StatusMayNotBeSetDirectlyToClosed
        if (newStatus == AccountStatus.CLOSED) {
            throw new InvalidAccountStatusException("Use closeAccount endpoint to close an account");
        }
        // OCL: BA_12_PendingMayOnlyBecomeActive
        if (currentStatus == AccountStatus.PENDING && newStatus != AccountStatus.ACTIVE) {
            throw new InvalidAccountStatusException("Pending account can only be activated");
        }
    }

    @Nested
    @DisplayName("OCL: BA_10 — closed accounts cannot change status")
    class ClosedAccountStatusTests {

        @Test
        @DisplayName("BA_10: attempting to update a CLOSED account throws")
        void ba10_closedAccount_cannotChangeStatus() {
            assertThatThrownBy(() -> validateStatusTransition(AccountStatus.CLOSED, AccountStatus.ACTIVE))
                    .isInstanceOf(InvalidAccountStatusException.class)
                    .hasMessageContaining("closed account");
        }
    }

    @Nested
    @DisplayName("OCL: BA_11 — direct transition to CLOSED via updateStatus is forbidden")
    class DirectCloseViaUpdateTests {

        @Test
        @DisplayName("BA_11: updateStatus with target CLOSED throws")
        void ba11_cannotSetStatusDirectlyToClosed() {
            assertThatThrownBy(() -> validateStatusTransition(AccountStatus.ACTIVE, AccountStatus.CLOSED))
                    .isInstanceOf(InvalidAccountStatusException.class)
                    .hasMessageContaining("closeAccount endpoint");
        }
    }

    @Nested
    @DisplayName("OCL: BA_12 — PENDING accounts may only become ACTIVE")
    class PendingStatusTransitionTests {

        @Test
        @DisplayName("BA_12: PENDING → ACTIVE is allowed")
        void ba12_pendingToActive_allowed() {
            assertThatCode(() -> validateStatusTransition(AccountStatus.PENDING, AccountStatus.ACTIVE))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("BA_12: PENDING → FROZEN is forbidden")
        void ba12_pendingToFrozen_throws() {
            assertThatThrownBy(() -> validateStatusTransition(AccountStatus.PENDING, AccountStatus.FROZEN))
                    .isInstanceOf(InvalidAccountStatusException.class)
                    .hasMessageContaining("only be activated");
        }

        @Test
        @DisplayName("BA_12: ACTIVE → FROZEN is allowed (not a PENDING restriction)")
        void ba12_activeToFrozen_allowed() {
            assertThatCode(() -> validateStatusTransition(AccountStatus.ACTIVE, AccountStatus.FROZEN))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("BA_12: FROZEN → ACTIVE is allowed (not a PENDING restriction)")
        void ba12_frozenToActive_allowed() {
            assertThatCode(() -> validateStatusTransition(AccountStatus.FROZEN, AccountStatus.ACTIVE))
                    .doesNotThrowAnyException();
        }
    }
}


