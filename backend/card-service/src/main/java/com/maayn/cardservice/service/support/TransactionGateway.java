package com.maayn.cardservice.service.support;

import com.maayn.cardservice.exception.TransactionGatewayException;
import maayn.veld.generated.sdk.transaction.TransactionClient;
import maayn.veld.generated.sdk.transaction.errors.SdkApiError;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionResponse;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import maayn.veld.generated.sdk.transaction.models.transaction.TransferRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
/**
 * Thin adapter around the generated transaction SDK.
 * It normalizes the transfer request shape the card service needs and translates SDK errors into domain-specific reasons.
 */
public class TransactionGateway {

    private final TransactionClient transactionClient;
    private final CardRulesValidator cardRulesValidator;
    //TODO: USE Dependency Injections like professionals
    public TransactionGateway(TransactionClient transactionClient, CardRulesValidator cardRulesValidator) {
        this.transactionClient = transactionClient;
        this.cardRulesValidator = cardRulesValidator;
    }

    public TransactionResponse transfer(
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currency,
            String idempotencyKey,
            TransactionType type
    ) throws Exception {
        TransferRequest transferRequest = new TransferRequest();
        String normalizedCurrency = cardRulesValidator.normalizeCurrency(currency);
        // Card flows always move the same currency on both sides because FX handling is not implemented here.
        transferRequest.setIdempotencyKey(idempotencyKey);
        transferRequest.setSourceAccountId(sourceAccountId);
        transferRequest.setDestinationAccountId(destinationAccountId);
        transferRequest.setAmount(amount);
        transferRequest.setCurrency(normalizedCurrency);
        transferRequest.setSourceCurrency(normalizedCurrency);
        transferRequest.setDestinationCurrency(normalizedCurrency);
        transferRequest.setType(type);

        try {
            return transactionClient.transaction.transfer(transferRequest);
        } catch (SdkApiError error) {
            // Reduce the SDK's wire-level errors to a small set of business reasons the card service can map cleanly.
            if ("INVALID_AMOUNT".equals(error.getCode())) {
                throw new TransactionGatewayException(TransactionGatewayException.Reason.INVALID_AMOUNT, error.getMessage());
            }
            if ("INSUFFICIENT_FUNDS".equals(error.getCode())) {
                throw new TransactionGatewayException(TransactionGatewayException.Reason.INSUFFICIENT_FUNDS, error.getMessage());
            }
            throw new TransactionGatewayException(TransactionGatewayException.Reason.UNKNOWN, error.getMessage());
        }
    }
}
