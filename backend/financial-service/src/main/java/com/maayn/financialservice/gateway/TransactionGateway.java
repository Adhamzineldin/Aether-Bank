package com.maayn.financialservice.gateway;

import com.maayn.financialservice.exceptions.DisbursementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
import maayn.veld.generated.sdk.transaction.TransactionClient;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionResponse;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import maayn.veld.generated.sdk.transaction.models.transaction.TransferRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionGateway {

    private final TransactionClient transactionClient;

    public TransactionResponse disburseLoan(UUID toAccount, BigDecimal amount, String currency, String idempotencyKey) {
        log.info("Disbursing loan to account {} amount {}", toAccount, amount);
        return transfer(SystemAccounts.CASH_VAULT_ID, toAccount, amount, currency,
                idempotencyKey, TransactionType.TRANSFER);
    }

    public TransactionResponse lockCertificateFunds(UUID fromAccount, BigDecimal principal, String currency, String idempotencyKey) {
        log.info("Locking certificate funds from account {} amount {}", fromAccount, principal);
        return transfer(fromAccount, SystemAccounts.CASH_VAULT_ID, principal, currency,
                idempotencyKey, TransactionType.INTERNAL_TRANSFER);
    }

    private TransactionResponse transfer(UUID from, UUID to, BigDecimal amount, String currency,
                                         String idempotencyKey, TransactionType type) {
        TransferRequest request = buildRequest(from, to, amount, currency, idempotencyKey, type);
        try {
            return transactionClient.transaction.transfer(request);
        } catch (Exception ex) {
            throw new DisbursementException("Transaction failed: " + ex.getMessage(), ex);
        }
    }

    private TransferRequest buildRequest(UUID from, UUID to, BigDecimal amount, String currency,
                                         String idempotencyKey, TransactionType type) {
        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(from);
        req.setDestinationAccountId(to);
        req.setAmount(amount);
        req.setCurrency(currency);
        req.setIdempotencyKey(idempotencyKey);
        req.setType(type);
        return req;
    }
}
