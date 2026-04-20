package com.maayn.financialservice.gateway;

import com.maayn.financialservice.exceptions.FinancialOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.sdk.card.CardClient;
import maayn.veld.generated.sdk.card.models.card.CardDetailsResponse;
import maayn.veld.generated.sdk.card.models.card.CardTransactionResponse;
import maayn.veld.generated.sdk.card.models.card.MerchantPaymentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardGateway {

    private final CardClient cardClient;

    public CardDetailsResponse getCardDetails(UUID cardId) {
        log.info("Fetching card details for cardId={}", cardId);
        try {
            return cardClient.card.getCardDetails(cardId.toString());
        } catch (Exception ex) {
            throw new FinancialOperationException(HttpStatus.BAD_GATEWAY, "Card service unavailable: " + ex.getMessage());
        }
    }

    public CardTransactionResponse processMerchantPayment(MerchantPaymentRequest request) {
        log.info("Processing merchant payment for cardToken={}", request.getCardToken());
        try {
            return cardClient.card.processMerchantPayment(request);
        } catch (Exception ex) {
            throw new FinancialOperationException(HttpStatus.BAD_GATEWAY, "Card payment failed: " + ex.getMessage());
        }
    }
}
