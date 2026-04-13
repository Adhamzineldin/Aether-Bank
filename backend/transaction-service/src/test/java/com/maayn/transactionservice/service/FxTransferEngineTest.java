package com.maayn.transactionservice.service;

import com.maayn.transactionservice.entity.LedgerAccountId;
import com.maayn.transactionservice.entity.LedgerBalance;
import com.maayn.transactionservice.repository.LedgerBalanceRepository;
import maayn.veld.generated.models.transaction.TransferRequest;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class FxTransferEngineTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LedgerBalanceRepository ledgerRepository;

    private final UUID adhamId = UUID.randomUUID();
    private final UUID momId = UUID.randomUUID();

    @BeforeEach
    void setupLiquidity() {
        // 1. Give Adham 100 USD
        upsertWallet(adhamId, "USD", "100.00");

        // 2. Give Mom an empty EUR wallet
        upsertWallet(momId, "EUR", "0.00");

        // 3. Fund the FX Market Maker (may already exist from BankVaultInitializer)
        upsertWallet(SystemAccounts.FX_MARKET_MAKER_ID, "USD", "1000.00");
        upsertWallet(SystemAccounts.FX_MARKET_MAKER_ID, "EUR", "1000.00");
    }

    @Test
    void executeFxTransfer_balancesArePerfectlyMaintained() throws Exception {
        // Arrange: Adham sends 50 USD to Mom (who receives EUR)
        TransferRequest request = new TransferRequest();
        request.setIdempotencyKey(UUID.randomUUID().toString());
        request.setSourceAccountId(adhamId);
        request.setDestinationAccountId(momId);
        request.setAmount(new BigDecimal("50.00"));
        request.setSourceCurrency("USD");
        request.setDestinationCurrency("EUR");

        // Act
        transactionService.transfer(request);

        // Assert
        // 1. Adham lost 50 USD (100 - 50 = 50)
        assertThat(getWalletBalance(adhamId, "USD")).isEqualByComparingTo("50.00");

        // 2. Mom gained 45 EUR (50 USD * 0.90 rate = 45 EUR)
        assertThat(getWalletBalance(momId, "EUR")).isEqualByComparingTo("45.00");

        // 3. The FX Desk perfectly balanced the two legs!
        // It gained the 50 USD from Adham
        assertThat(getWalletBalance(SystemAccounts.FX_MARKET_MAKER_ID, "USD")).isEqualByComparingTo("1050.00");
        // It lost the 45 EUR it gave to Mom
        assertThat(getWalletBalance(SystemAccounts.FX_MARKET_MAKER_ID, "EUR")).isEqualByComparingTo("955.00");
    }

    // Helper: find existing wallet and set balance, or create new
    private void upsertWallet(UUID id, String currency, String amount) {
        LedgerAccountId key = new LedgerAccountId(id, currency);
        Optional<LedgerBalance> existing = ledgerRepository.findById(key);

        LedgerBalance wallet;
        if (existing.isPresent()) {
            wallet = existing.get();
            wallet.setAvailableBalance(new BigDecimal(amount));
        } else {
            wallet = new LedgerBalance(id, currency);
            wallet.credit(new BigDecimal(amount));
        }
        ledgerRepository.saveAndFlush(wallet);
    }

    private BigDecimal getWalletBalance(UUID id, String currency) {
        return ledgerRepository.findById(new LedgerAccountId(id, currency))
                .orElseThrow(() -> new AssertionError("Wallet not found: " + id + "/" + currency))
                .getAvailableBalance();
    }
}

