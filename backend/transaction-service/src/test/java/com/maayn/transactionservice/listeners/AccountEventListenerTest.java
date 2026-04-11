package com.maayn.transactionservice.listeners;

import com.maayn.transactionservice.entity.LedgerAccountId;
import com.maayn.transactionservice.entity.LedgerBalance;
import com.maayn.transactionservice.repository.LedgerBalanceRepository;
import maayn.veld.generated.sdk.account.models.shared.AccountCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountEventListener Unit Tests — RabbitMQ Integration Simulation")
class AccountEventListenerTest {

    @Mock private LedgerBalanceRepository ledgerBalanceRepository;

    @InjectMocks private AccountEventListener listener;

    // ════════════════════════════════════════════════════════════════
    //  handleAccountCreated() — simulates Account Service → RabbitMQ → Transaction Service
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When Account Service sends AccountCreatedEvent via RabbitMQ")
    class AccountCreatedEventTests {

        @Test
        @DisplayName("Should create a new ledger balance when account does not exist")
        void handleAccountCreated_newAccount_createsLedger() {
            UUID accountId = UUID.randomUUID();
            AccountCreatedEvent event = new AccountCreatedEvent(accountId, "USD", LocalDateTime.now());

            when(ledgerBalanceRepository.existsById(new LedgerAccountId(accountId, "USD"))).thenReturn(false);

            listener.handleAccountCreated(event);

            ArgumentCaptor<LedgerBalance> captor = ArgumentCaptor.forClass(LedgerBalance.class);
            verify(ledgerBalanceRepository).save(captor.capture());

            LedgerBalance saved = captor.getValue();
            assertThat(saved.getId().getAccountId()).isEqualTo(accountId);
            assertThat(saved.getAvailableBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(saved.getPendingHolds()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should NOT create duplicate ledger when account already exists")
        void handleAccountCreated_existingAccount_skips() {
            UUID accountId = UUID.randomUUID();
            AccountCreatedEvent event = new AccountCreatedEvent(accountId, "USD", LocalDateTime.now());

            when(ledgerBalanceRepository.existsById(new LedgerAccountId(accountId, "USD"))).thenReturn(true);

            listener.handleAccountCreated(event);

            verify(ledgerBalanceRepository, never()).save(any(LedgerBalance.class));
        }

        @Test
        @DisplayName("Should handle event with EUR currency")
        void handleAccountCreated_eurCurrency() {
            UUID accountId = UUID.randomUUID();
            AccountCreatedEvent event = new AccountCreatedEvent(accountId, "EUR", LocalDateTime.now());

            when(ledgerBalanceRepository.existsById(new LedgerAccountId(accountId, "EUR"))).thenReturn(false);

            listener.handleAccountCreated(event);

            verify(ledgerBalanceRepository).save(any(LedgerBalance.class));
        }

        @Test
        @DisplayName("Should process concurrent account creation events idempotently")
        void handleAccountCreated_duplicateEvents_onlyCreatesOnce() {
            UUID accountId = UUID.randomUUID();
            AccountCreatedEvent event = new AccountCreatedEvent(accountId, "USD", LocalDateTime.now());

            // First call: account doesn't exist
            when(ledgerBalanceRepository.existsById(new LedgerAccountId(accountId, "USD"))).thenReturn(false);
            listener.handleAccountCreated(event);

            // Second call: account now exists
            when(ledgerBalanceRepository.existsById(new LedgerAccountId(accountId, "USD"))).thenReturn(true);
            listener.handleAccountCreated(event);

            verify(ledgerBalanceRepository, times(1)).save(any(LedgerBalance.class));
        }

        @Test
        @DisplayName("Should initialize balance to exactly zero for new accounts")
        void handleAccountCreated_balanceIsExactlyZero() {
            UUID accountId = UUID.randomUUID();
            AccountCreatedEvent event = new AccountCreatedEvent(accountId, "USD", LocalDateTime.now());

            when(ledgerBalanceRepository.existsById(new LedgerAccountId(accountId, "USD"))).thenReturn(false);

            listener.handleAccountCreated(event);

            ArgumentCaptor<LedgerBalance> captor = ArgumentCaptor.forClass(LedgerBalance.class);
            verify(ledgerBalanceRepository).save(captor.capture());

            LedgerBalance saved = captor.getValue();
            assertThat(saved.getAvailableBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(saved.getPendingHolds()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle multiple different account creation events")
        void handleAccountCreated_multipleAccounts() {
            UUID accountId1 = UUID.randomUUID();
            UUID accountId2 = UUID.randomUUID();

            AccountCreatedEvent event1 = new AccountCreatedEvent(accountId1, "USD", LocalDateTime.now());
            AccountCreatedEvent event2 = new AccountCreatedEvent(accountId2, "GBP", LocalDateTime.now());

            when(ledgerBalanceRepository.existsById(any(LedgerAccountId.class))).thenReturn(false);

            listener.handleAccountCreated(event1);
            listener.handleAccountCreated(event2);

            verify(ledgerBalanceRepository, times(2)).save(any(LedgerBalance.class));
        }
    }
}

