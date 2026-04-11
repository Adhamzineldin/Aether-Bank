package com.maayn.transactionservice.service;

import com.maayn.transactionservice.entity.LedgerAccountId;
import com.maayn.transactionservice.entity.LedgerBalance;
import com.maayn.transactionservice.exceptions.LedgerNotInitializedException;
import com.maayn.transactionservice.repository.LedgerBalanceRepository;
import maayn.veld.generated.errors.GetAccountBalanceException;
import maayn.veld.generated.models.ledger.BalanceResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LedgerService Unit Tests")
class LedgerServiceTest {

    @Mock private LedgerBalanceRepository ledgerBalanceRepository;

    @InjectMocks private LedgerService ledgerService;

    private UUID accountId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
    }

    // ════════════════════════════════════════════════════════════════
    //  getAccountBalance()
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAccountBalance()")
    class GetAccountBalanceTests {

        @Test
        @DisplayName("Should return balance when ledger exists for the account")
        void getAccountBalance_success() throws Exception {
            LedgerBalance balance = new LedgerBalance(accountId, "USD");
            balance.setAvailableBalance(new BigDecimal("500.00"));
            balance.setPendingHolds(BigDecimal.ZERO);

            when(ledgerBalanceRepository.findById(new LedgerAccountId(accountId, "USD")))
                    .thenReturn(Optional.of(balance));

            BalanceResponse response = ledgerService.getAccountBalance(accountId.toString(), "USD");

            assertThat(response).isNotNull();
            assertThat(response.getAccountId()).isEqualTo(accountId);
            assertThat(response.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(response.getPendingHolds()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should throw exception when ledger not initialized")
        void getAccountBalance_notInitialized_throwsException() {
            when(ledgerBalanceRepository.findById(new LedgerAccountId(accountId, "USD")))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ledgerService.getAccountBalance(accountId.toString(), "USD"))
                    .isInstanceOf(GetAccountBalanceException.class)
                    .hasMessageContaining("Ledger balance not initialized");
        }

        @Test
        @DisplayName("Should throw exception for invalid UUID format")
        void getAccountBalance_invalidUUID_throwsException() {
            assertThatThrownBy(() -> ledgerService.getAccountBalance("not-a-uuid", "USD"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception for empty account ID")
        void getAccountBalance_emptyAccountId_throwsException() {
            assertThatThrownBy(() -> ledgerService.getAccountBalance("", "USD"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception for null-like account ID")
        void getAccountBalance_nullString_throwsException() {
            assertThatThrownBy(() -> ledgerService.getAccountBalance("null", "USD"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should return zero balance for newly initialized ledger")
        void getAccountBalance_zeroBalance() throws Exception {
            LedgerBalance balance = new LedgerBalance(accountId, "USD");

            when(ledgerBalanceRepository.findById(new LedgerAccountId(accountId, "USD")))
                    .thenReturn(Optional.of(balance));

            BalanceResponse response = ledgerService.getAccountBalance(accountId.toString(), "USD");

            assertThat(response.getAvailableBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getPendingHolds()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  executeTransferMath()
    // ════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("executeTransferMath()")
    class ExecuteTransferMathTests {

        private UUID sourceId;
        private UUID destId;

        @BeforeEach
        void setUp() {
            sourceId = UUID.randomUUID();
            destId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should debit source and credit destination for valid transfer")
        void executeTransferMath_success() {
            LedgerBalance source = new LedgerBalance(sourceId, "USD");
            source.setAvailableBalance(new BigDecimal("1000.00"));

            LedgerBalance dest = new LedgerBalance(destId, "USD");
            dest.setAvailableBalance(new BigDecimal("200.00"));

            when(ledgerBalanceRepository.findById(new LedgerAccountId(sourceId, "USD")))
                    .thenReturn(Optional.of(source));
            when(ledgerBalanceRepository.findById(new LedgerAccountId(destId, "USD")))
                    .thenReturn(Optional.of(dest));

            ledgerService.executeTransferMath(sourceId, destId, new BigDecimal("300.00"), "USD");

            assertThat(source.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
            assertThat(dest.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("500.00"));

            verify(ledgerBalanceRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Should throw exception when source has insufficient funds")
        void executeTransferMath_insufficientFunds() {
            LedgerBalance source = new LedgerBalance(sourceId, "USD");
            source.setAvailableBalance(new BigDecimal("50.00"));

            LedgerBalance dest = new LedgerBalance(destId, "USD");
            dest.setAvailableBalance(new BigDecimal("200.00"));

            when(ledgerBalanceRepository.findById(new LedgerAccountId(sourceId, "USD")))
                    .thenReturn(Optional.of(source));
            when(ledgerBalanceRepository.findById(new LedgerAccountId(destId, "USD")))
                    .thenReturn(Optional.of(dest));

            assertThatThrownBy(() -> ledgerService.executeTransferMath(sourceId, destId, new BigDecimal("100.00"), "USD"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient funds");

            // Destination should NOT have been credited
            assertThat(dest.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("Should throw LedgerNotInitializedException for non-existing source account")
        void executeTransferMath_newSourceAccount() {
            when(ledgerBalanceRepository.findById(new LedgerAccountId(sourceId, "USD")))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ledgerService.executeTransferMath(sourceId, destId, new BigDecimal("100.00"), "USD"))
                    .isInstanceOf(LedgerNotInitializedException.class);
        }

        @Test
        @DisplayName("Should throw LedgerNotInitializedException for non-existing destination account")
        void executeTransferMath_newDestinationAccount() {
            LedgerBalance source = new LedgerBalance(sourceId, "USD");
            source.setAvailableBalance(new BigDecimal("1000.00"));

            when(ledgerBalanceRepository.findById(new LedgerAccountId(sourceId, "USD")))
                    .thenReturn(Optional.of(source));
            when(ledgerBalanceRepository.findById(new LedgerAccountId(destId, "USD")))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ledgerService.executeTransferMath(sourceId, destId, new BigDecimal("100.00"), "USD"))
                    .isInstanceOf(LedgerNotInitializedException.class)
                    .hasMessageContaining(destId.toString());

            verify(ledgerBalanceRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("Should handle transfer of exact source balance (drain to zero)")
        void executeTransferMath_drainToZero() {
            LedgerBalance source = new LedgerBalance(sourceId, "USD");
            source.setAvailableBalance(new BigDecimal("500.00"));

            LedgerBalance dest = new LedgerBalance(destId, "USD");
            dest.setAvailableBalance(new BigDecimal("0.00"));

            when(ledgerBalanceRepository.findById(new LedgerAccountId(sourceId, "USD")))
                    .thenReturn(Optional.of(source));
            when(ledgerBalanceRepository.findById(new LedgerAccountId(destId, "USD")))
                    .thenReturn(Optional.of(dest));

            ledgerService.executeTransferMath(sourceId, destId, new BigDecimal("500.00"), "USD");

            assertThat(source.getAvailableBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(dest.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("Should correctly transfer very small amounts")
        void executeTransferMath_smallAmount() {
            LedgerBalance source = new LedgerBalance(sourceId, "USD");
            source.setAvailableBalance(new BigDecimal("1.00"));

            LedgerBalance dest = new LedgerBalance(destId, "USD");
            dest.setAvailableBalance(new BigDecimal("0.00"));

            when(ledgerBalanceRepository.findById(new LedgerAccountId(sourceId, "USD")))
                    .thenReturn(Optional.of(source));
            when(ledgerBalanceRepository.findById(new LedgerAccountId(destId, "USD")))
                    .thenReturn(Optional.of(dest));

            ledgerService.executeTransferMath(sourceId, destId, new BigDecimal("0.01"), "USD");

            assertThat(source.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("0.99"));
            assertThat(dest.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("0.01"));
        }

        @Test
        @DisplayName("Should save both balances in a single saveAll call")
        void executeTransferMath_savesBothBalancesAtOnce() {
            LedgerBalance source = new LedgerBalance(sourceId, "USD");
            source.setAvailableBalance(new BigDecimal("1000.00"));

            LedgerBalance dest = new LedgerBalance(destId, "USD");
            dest.setAvailableBalance(new BigDecimal("200.00"));

            when(ledgerBalanceRepository.findById(new LedgerAccountId(sourceId, "USD")))
                    .thenReturn(Optional.of(source));
            when(ledgerBalanceRepository.findById(new LedgerAccountId(destId, "USD")))
                    .thenReturn(Optional.of(dest));

            ledgerService.executeTransferMath(sourceId, destId, new BigDecimal("100.00"), "USD");

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<LedgerBalance>> captor = ArgumentCaptor.forClass(List.class);
            verify(ledgerBalanceRepository).saveAll(captor.capture());

            assertThat(captor.getValue()).hasSize(2);
        }
    }
}

