package com.maayn.transactionservice.mappers;

import com.maayn.transactionservice.entity.LedgerBalance;
import maayn.veld.generated.models.ledger.BalanceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LedgerMapper Unit Tests")
class LedgerMapperTest {

    @Test
    @DisplayName("Should map LedgerBalance to BalanceResponse correctly")
    void toBalanceResponse_mapsAllFields() {
        UUID accountId = UUID.randomUUID();
        LedgerBalance balance = new LedgerBalance(accountId);
        balance.setAvailableBalance(new BigDecimal("1234.56"));
        balance.setPendingHolds(new BigDecimal("50.00"));

        BalanceResponse response = LedgerMapper.toBalanceResponse(balance);

        assertThat(response.getAccountId()).isEqualTo(accountId);
        assertThat(response.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("1234.56"));
        assertThat(response.getPendingHolds()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Should handle zero balances")
    void toBalanceResponse_zeroBalances() {
        UUID accountId = UUID.randomUUID();
        LedgerBalance balance = new LedgerBalance(accountId);

        BalanceResponse response = LedgerMapper.toBalanceResponse(balance);

        assertThat(response.getAvailableBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getPendingHolds()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should preserve large balance values")
    void toBalanceResponse_largeBalance() {
        UUID accountId = UUID.randomUUID();
        LedgerBalance balance = new LedgerBalance(accountId);
        balance.setAvailableBalance(new BigDecimal("99999999999.99"));
        balance.setPendingHolds(new BigDecimal("12345678.01"));

        BalanceResponse response = LedgerMapper.toBalanceResponse(balance);

        assertThat(response.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("99999999999.99"));
        assertThat(response.getPendingHolds()).isEqualByComparingTo(new BigDecimal("12345678.01"));
    }
}

