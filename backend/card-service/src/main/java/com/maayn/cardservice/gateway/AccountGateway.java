package com.maayn.cardservice.gateway;

import com.maayn.cardservice.exception.TransactionGatewayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.sdk.account.AccountClient;
import maayn.veld.generated.sdk.account.models.shared.AccountCreatedEvent;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
import maayn.veld.generated.sdk.transaction.constants.TransactionRabbitConfig;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountGateway {

    private static final int MAX_FUND_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 200;

    private final AccountClient accountClient;
    private final TransactionGateway transactionGateway;
    private final RabbitTemplate rabbitTemplate;

    public void verifyDebitAccountExists(UUID accountId) {
        try {
            boolean exists = accountClient.account.doesAccountExist(accountId.toString());
            if (!exists) throw new IllegalArgumentException("Account not found: " + accountId);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to verify account: " + accountId, ex);
        }
    }

    public UUID provisionCreditAccount(BigDecimal creditLimit, String currency) {
        UUID accountId = UUID.randomUUID();
        publishAccountCreatedEvent(accountId, currency);
        fundCreditAccountWithRetry(accountId, creditLimit, currency);
        return accountId;
    }

    private void publishAccountCreatedEvent(UUID accountId, String currency) {
        AccountCreatedEvent event = new AccountCreatedEvent(accountId, currency, LocalDateTime.now());
        rabbitTemplate.convertAndSend(TransactionRabbitConfig.ACCOUNT_EVENTS_QUEUE, event);
        log.info("Published AccountCreatedEvent for credit account: {}", accountId);
    }

    private void fundCreditAccountWithRetry(UUID accountId, BigDecimal creditLimit, String currency) {
        String idempotencyKey = "credit-fund-" + accountId;
        for (int attempt = 1; attempt <= MAX_FUND_RETRIES; attempt++) {
            try {
                transactionGateway.transfer(
                        SystemAccounts.CASH_VAULT_ID, accountId,
                        creditLimit, currency, idempotencyKey, TransactionType.INTERNAL_TRANSFER);
                return;
            } catch (TransactionGatewayException ex) {
                if (ex.getReason() != TransactionGatewayException.Reason.UNKNOWN || attempt == MAX_FUND_RETRIES) throw ex;
                sleepQuietly(RETRY_DELAY_MS * attempt);
            }
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
