package com.maayn.financialservice.gateway;

import com.maayn.financialservice.exceptions.DisbursementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import maayn.veld.generated.sdk.transaction.models.transaction.TransferRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Publishes ledger transfer commands to the transaction-service asynchronously
 * via RabbitMQ on the {@code ledger.commands.queue} (defined in
 * transaction-service's {@code TransactionRabbitConfig.SAGA_COMMANDS_QUEUE}).
 *
 * <p>Previously this gateway invoked {@code TransactionClient} over HTTP, which
 * coupled financial-service to transaction-service availability and produced
 * {@code ConnectException}s during disbursement when the downstream was
 * unreachable. The transaction-service already exposes
 * {@code LedgerCommandListener} on the {@code ledger.commands.queue}, so the
 * correct integration is fire-and-forget messaging; the broker buffers commands
 * if the consumer is down and {@code TransferFailedEvent} is emitted back on
 * the banking exchange when a command can't be applied.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionGateway {

    /**
     * Mirror of {@code maayn.veld.generated.constants.TransactionRabbitConfig
     * .SAGA_COMMANDS_QUEUE} from transaction-service. Inlined here because the
     * constant isn't included in financial-service's veld SDK bundle. Keep the
     * two values in sync.
     */
    static final String LEDGER_COMMANDS_QUEUE = "ledger.commands.queue";

    private final RabbitTemplate rabbitTemplate;

    public void disburseLoan(UUID toAccount, BigDecimal amount, String currency, String idempotencyKey) {
        log.info("Dispatching loan disbursement command to ledger: account={} amount={} {}",
                toAccount, amount, currency);
        publish(SystemAccounts.CASH_VAULT_ID, toAccount, amount, currency,
                idempotencyKey, TransactionType.TRANSFER);
    }

    public void lockCertificateFunds(UUID fromAccount, BigDecimal principal, String currency, String idempotencyKey) {
        log.info("Dispatching certificate fund-lock command to ledger: account={} amount={} {}",
                fromAccount, principal, currency);
        publish(fromAccount, SystemAccounts.CASH_VAULT_ID, principal, currency,
                idempotencyKey, TransactionType.INTERNAL_TRANSFER);
    }

    private void publish(UUID from, UUID to, BigDecimal amount, String currency,
                         String idempotencyKey, TransactionType type) {
        TransferRequest request = buildRequest(from, to, amount, currency, idempotencyKey, type);
        try {
            // Use the default exchange ("") with the queue name as routing key
            // so the message is delivered straight to LedgerCommandListener
            // without requiring a binding declaration on this side.
            rabbitTemplate.convertAndSend("", LEDGER_COMMANDS_QUEUE, request);
            log.debug("Published transfer command idempotencyKey={} from={} to={} amount={} {}",
                    idempotencyKey, from, to, amount, currency);
        } catch (Exception ex) {
            // Publishing to RabbitMQ should only fail if the broker itself is
            // unreachable; surface that as a DisbursementException so callers
            // can NACK the inbound RabbitMQ message and retry later.
            throw new DisbursementException("Failed to publish ledger transfer command: " + ex.getMessage(), ex);
        }
    }

    private TransferRequest buildRequest(UUID from, UUID to, BigDecimal amount, String currency,
                                         String idempotencyKey, TransactionType type) {
        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(from);
        req.setDestinationAccountId(to);
        req.setAmount(amount);
        req.setCurrency(currency);
        req.setSourceCurrency(currency);
        req.setDestinationCurrency(currency);
        req.setIdempotencyKey(idempotencyKey);
        req.setType(type);
        return req;
    }
}
