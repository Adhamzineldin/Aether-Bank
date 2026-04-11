package com.maayn.transactionservice.config;

import com.maayn.transactionservice.entity.LedgerAccountId;
import com.maayn.transactionservice.entity.LedgerBalance;
import com.maayn.transactionservice.repository.LedgerBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BankVaultInitializer {

    private final LedgerBalanceRepository repository;
    
    //TODO: make it dynamic by fetching supported currencies from a config service or database
    private static final List<String> SUPPORTED_CURRENCIES = List.of("USD", "EUR", "EGP");

    private static final BigDecimal DEFAULT_LIQUIDITY = new BigDecimal("1000000000.00");

    @Bean
    public CommandLineRunner initializeSystemAccounts() {
        return args -> {
            log.info("Checking System Accounts for supported currencies...");

            for (String currency : SUPPORTED_CURRENCIES) {
                initSystemAccount(SystemAccounts.CASH_VAULT_ID, "Cash Vault", currency, DEFAULT_LIQUIDITY);

                initSystemAccount(SystemAccounts.FX_MARKET_MAKER_ID, "FX Desk", currency, DEFAULT_LIQUIDITY);
            }
        };
    }

    private void initSystemAccount(UUID accountId, String accountName, String currency, BigDecimal initialAmount) {
        LedgerAccountId id = new LedgerAccountId(accountId, currency);

        if (!repository.existsById(id)) {
            LedgerBalance balance = new LedgerBalance(accountId, currency);
            balance.credit(initialAmount);
            repository.save(balance);

            log.info("System Account '{}' [{}] initialized with {}", accountName, currency, initialAmount);
        }
    }
}