package com.maayn.accountservice.config;

import com.maayn.accountservice.enums.AccountStatus;
import com.maayn.accountservice.enums.AccountType;
import com.maayn.accountservice.events.AccountCreatedEvent;
import com.maayn.accountservice.events.AccountEventPublisher;
import com.maayn.accountservice.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Seeds three demo bank accounts owned by the bootstrap superadmin so the
 * system is usable for transfers immediately after first boot.
 *
 * Fixed UUIDs let the transaction-service pre-credit these accounts in its own
 * seeder without any inter-service calls at startup time.
 *
 * A direct SQL INSERT is used (rather than the JPA repository) because the
 * BankAccount entity is mapped with {@code @GeneratedValue(GenerationType.UUID)}.
 * Under Hibernate 7, passing a pre-assigned ID to {@code persist()} raises
 * {@code PersistentObjectException}, and routing through {@code merge()} raises
 * {@code StaleObjectStateException} when the row does not yet exist. Seeding via
 * JDBC sidesteps the JPA identifier lifecycle entirely.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DemoAccountSeeder {

    /** Must match IAM-service DataSeeder.SUPERADMIN_ID */
    public static final UUID SUPERADMIN_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    /** Well-known account IDs — shared with transaction-service BankVaultInitializer */
    public static final UUID DEMO_USD_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    public static final UUID DEMO_EUR_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    public static final UUID DEMO_EGP_ID = UUID.fromString("00000000-0000-0000-0000-000000000012");

    private record DemoAccount(UUID id, String accountNumber, AccountType type, String currency) {}

    private static final List<DemoAccount> DEMO_ACCOUNTS = List.of(
            new DemoAccount(DEMO_USD_ID, "DEMO-CHK-USD-001", AccountType.CHECKING,   "USD"),
            new DemoAccount(DEMO_EUR_ID, "DEMO-CHK-EUR-001", AccountType.CHECKING,   "EUR"),
            new DemoAccount(DEMO_EGP_ID, "DEMO-SAV-EGP-001", AccountType.SAVINGS,    "EGP")
    );

    private static final String INSERT_SQL = """
            INSERT INTO bank_accounts
                (id, account_number, customer_id, account_type, status,
                 currency, opened_date, closed_date, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, NULL, ?, ?)
            """;

    @Bean
    public CommandLineRunner seedDemoAccounts(
            BankAccountRepository bankAccountRepository,
            AccountEventPublisher eventPublisher,
            JdbcTemplate jdbcTemplate
    ) {
        return args -> {
            for (DemoAccount demo : DEMO_ACCOUNTS) {
                if (bankAccountRepository.existsById(demo.id())) {
                    log.debug("Demo account {} ({}) already exists — skipping.", demo.accountNumber(), demo.currency());
                    continue;
                }

                LocalDateTime now = LocalDateTime.now();
                jdbcTemplate.update(INSERT_SQL,
                        demo.id(),
                        demo.accountNumber(),
                        SUPERADMIN_ID,
                        AccountType.CHECKING.equals(demo.type()) ? "CHECKING" : demo.type().name(),
                        AccountStatus.ACTIVE.name(),
                        demo.currency(),
                        java.sql.Date.valueOf(LocalDate.now()),
                        Timestamp.valueOf(now),
                        Timestamp.valueOf(now)
                );

                // Publish so the transaction-service initializes a ledger entry.
                // BankVaultInitializer pre-credits these accounts so if the ledger
                // already exists the AccountEventListener will just skip.
                eventPublisher.publishAccountCreated(
                        AccountCreatedEvent.builder()
                                .accountId(demo.id())
                                .currency(demo.currency())
                                .timestamp(now)
                                .build()
                );

                log.info("Seeded demo account {} [{}] → customerId={}",
                        demo.accountNumber(), demo.currency(), SUPERADMIN_ID);
            }
        };
    }
}
