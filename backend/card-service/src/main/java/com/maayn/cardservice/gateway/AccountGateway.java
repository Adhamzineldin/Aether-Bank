package com.maayn.cardservice.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maayn.cardservice.exception.TransactionGatewayException;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.sdk.account.AccountClient;
import maayn.veld.generated.sdk.account.models.shared.AccountCreatedEvent;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
import maayn.veld.generated.sdk.transaction.constants.TransactionRabbitConfig;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class AccountGateway {

    private static final int MAX_FUND_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 200;

    private final AccountClient accountClient;
    private final TransactionGateway transactionGateway;
    private final RabbitTemplate rabbitTemplate;
    private final String accountServiceBaseUrl;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public AccountGateway(
            AccountClient accountClient,
            TransactionGateway transactionGateway,
            RabbitTemplate rabbitTemplate,
            @Value("${veld.sdk.account.base-url:${VELD_ACCOUNT_URL:http://account-service:3003}}") String accountServiceBaseUrl
    ) {
        this.accountClient = accountClient;
        this.transactionGateway = transactionGateway;
        this.rabbitTemplate = rabbitTemplate;
        this.accountServiceBaseUrl = accountServiceBaseUrl;
    }

    public void verifyDebitAccountExists(UUID accountId) {
        try {
            boolean exists = accountClient.account.doesAccountExist(accountId.toString());
            if (!exists) throw new IllegalArgumentException("Account not found: " + accountId);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to verify account: " + accountId, ex);
        }
    }

    /**
     * Returns the ISO-4217 currency the linked account is denominated in.
     * Used at debit-card issuance and as a back-fill for legacy card rows on
     * the merchant payment path so transfers always target the correct
     * {@code (accountId, currency)} ledger key.
     */
    public String fetchAccountCurrency(UUID accountId) {
        // The Veld-generated SDK's AccountResponse expects a nested
        // {account: {currency, ...}} envelope, but account-service's
        // BankAccountController returns a *flat* DTO ({id, currency, ...}),
        // so the SDK call always yields a null `account` and we'd report
        // "Account currency unavailable". Read the raw JSON instead and
        // pull `currency` straight off the root, with a SDK fallback for
        // forward compatibility if the controller ever migrates to the
        // wrapped shape.
        String url = accountServiceBaseUrl + "/api/accounts_service/account/" + accountId;
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 400) {
                log.warn("Account lookup HTTP {} from {} body={}", res.statusCode(), url, truncate(res.body()));
                throw new IllegalArgumentException("Account lookup failed (" + res.statusCode() + ") for: " + accountId);
            }
            JsonNode root = mapper.readTree(res.body());
            String currency = textAt(root, "currency");
            if (currency == null) {
                // legacy / future wrapped shape: { account: { currency } }
                currency = textAt(root.path("account"), "currency");
            }
            if (currency == null || currency.isBlank()) {
                // Surface the raw payload so we can tell whether the controller
                // actually returned the account row (and currency was just null)
                // versus an unexpected shape (HTML error page, different schema,
                // wrong base URL hitting some other service, etc.). Most common
                // cause in dev is VELD_ACCOUNT_URL pointing at the docker
                // hostname `account-service` while running outside docker —
                // that would actually throw IOException above, so a 200 with
                // missing currency typically means the row was created without
                // a currency column populated.
                log.warn("Account currency missing in response from {} body={}", url, truncate(res.body()));
                throw new IllegalArgumentException("Account currency unavailable for: " + accountId);
            }
            return currency;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Account currency lookup failed for {} url={} err={}", accountId, url, ex.toString());
            throw new IllegalArgumentException("Failed to fetch account currency: " + accountId, ex);
        }
    }

    private static String truncate(String s) {
        if (s == null) return "<null>";
        return s.length() > 500 ? s.substring(0, 500) + "…" : s;
    }

    private static String textAt(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asText(null);
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
