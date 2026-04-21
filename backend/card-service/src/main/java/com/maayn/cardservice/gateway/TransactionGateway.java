package com.maayn.cardservice.gateway;

import com.maayn.cardservice.exception.TransactionGatewayException;
import com.maayn.cardservice.validator.CardRulesValidator;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.sdk.transaction.TransactionClient;
import maayn.veld.generated.sdk.transaction.errors.SdkApiError;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionResponse;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import maayn.veld.generated.sdk.transaction.models.transaction.TransferRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionGateway {

    private final TransactionClient transactionClient;
    private final CardRulesValidator cardRulesValidator;

    public TransactionResponse transfer(
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currency,
            String idempotencyKey,
            TransactionType type
    ) {
        String normalizedCurrency = cardRulesValidator.normalizeCurrency(currency);
        TransferRequest request = buildTransferRequest(sourceAccountId, destinationAccountId, amount, normalizedCurrency, idempotencyKey, type);
        try {
            return transactionClient.transaction.transfer(request);
        } catch (SdkApiError error) {
            throw mapSdkError(error);
        } catch (Exception ex) {
            throw new TransactionGatewayException("Transaction transfer failed: " + ex.getMessage(), ex);
        }
    }

    private TransferRequest buildTransferRequest(UUID source, UUID dest, BigDecimal amount, String currency, String key, TransactionType type) {
        TransferRequest request = new TransferRequest();
        request.setIdempotencyKey(key);
        request.setSourceAccountId(source);
        request.setDestinationAccountId(dest);
        request.setAmount(amount);
        request.setCurrency(currency);
        request.setSourceCurrency(currency);
        request.setDestinationCurrency(currency);
        request.setType(type);
        return request;
    }

    private TransactionGatewayException mapSdkError(SdkApiError error) {
        return switch (error.getCode()) {
            case "INVALID_AMOUNT" -> new TransactionGatewayException(TransactionGatewayException.Reason.INVALID_AMOUNT, error.getMessage());
            case "INSUFFICIENT_FUNDS" -> new TransactionGatewayException(TransactionGatewayException.Reason.INSUFFICIENT_FUNDS, error.getMessage());
            default -> new TransactionGatewayException(TransactionGatewayException.Reason.UNKNOWN, error.getMessage());
        };
    }
}
