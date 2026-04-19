package com.maayn.cardservice.PaymentFlows;

import com.maayn.cardservice.entity.Card;
import maayn.veld.generated.models.card.CardTransactionResponse;

import java.math.BigDecimal;

/**
 * Contract for payment flow implementations (SOLID: Interface Segregation).
 * Enables different payment processing strategies (Credit/Debit).
 */
public interface PaymentFlow {

    /**
     * Processes a merchant payment for a card.
     * 
     * @param card the card being used
     * @param merchantId the merchant UUID
     * @param iban merchant IBAN for payment routing
     * @param cvv CVV for validation
     * @param expiryDate card expiry in MM/YY format
     * @param amount payment amount
     * @param currency currency code (e.g., "USD")
     * @param idempotencyKey unique key for idempotency
     */
    CardTransactionResponse processPayment(
        Card card,
        String merchantId,
        String iban,
        String cvv,
        String expiryDate,
        BigDecimal amount,
        String currency,
        String idempotencyKey
    ) throws Exception;

    /**
     * Identifies the payment flow type.
     */
    PaymentFlowType getFlowType();
}
