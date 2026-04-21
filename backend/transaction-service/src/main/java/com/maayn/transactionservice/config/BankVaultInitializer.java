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
    
    private static final List<String> SUPPORTED_CURRENCIES = List.of("USD", "EUR", "EGP");

    private static final BigDecimal DEFAULT_LIQUIDITY = new BigDecimal("1000000000.00");

    /** Starting balance for each superadmin demo account — 1 billion so any demo transfer works. */
    private static final BigDecimal DEMO_BALANCE = new BigDecimal("1000000000.00");

    // Fixed UUIDs — must match account-service DemoAccountSeeder
    private static final UUID SUPERADMIN_DEMO_USD = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID SUPERADMIN_DEMO_EUR = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID SUPERADMIN_DEMO_EGP = UUID.fromString("00000000-0000-0000-0000-000000000012");

    @Bean
    public CommandLineRunner initializeSystemAccounts() {
        return args -> {
            log.info("Checking system accounts for supported currencies…");

            for (String currency : SUPPORTED_CURRENCIES) {
                initSystemAccount(SystemAccounts.CASH_VAULT_ID,      "Cash Vault", currency, DEFAULT_LIQUIDITY);
                initSystemAccount(SystemAccounts.FX_MARKET_MAKER_ID, "FX Desk",    currency, DEFAULT_LIQUIDITY);
            }

            // Pre-credit the superadmin's demo accounts so they have spendable
            // funds from the first login. The AccountEventListener's existsById
            // check prevents these from being overwritten when the account-service
            // publishes AccountCreatedEvents later.
            initSystemAccount(SUPERADMIN_DEMO_USD, "Demo USD Checking", "USD", DEMO_BALANCE);
            initSystemAccount(SUPERADMIN_DEMO_EUR, "Demo EUR Checking", "EUR", DEMO_BALANCE);
            initSystemAccount(SUPERADMIN_DEMO_EGP, "Demo EGP Savings",  "EGP", DEMO_BALANCE);
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