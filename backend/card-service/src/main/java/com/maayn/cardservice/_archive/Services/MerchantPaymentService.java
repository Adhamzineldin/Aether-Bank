package com.maayn.cardservice.Services;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.PaymentFlows.PaymentFlowFactory;
import com.maayn.cardservice.PaymentFlows.PaymentFlow;
import com.maayn.cardservice.repository.CardRepository;
import com.maayn.cardservice.repository.CardTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.card.CardTransactionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Merchant Payment Service (SOLID: Single Responsibility + Dependency Inversion).
 * Orchestrates payment flow selection and processing.
 * Depends on abstractions (PaymentFlow) not concretions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantPaymentService {

    private final PaymentFlowFactory paymentFlowFactory;
    private final CardAccessService cardAccessService;
    private final CardRepository cardRepository;
    private final CardTransactionRepository cardTransactionRepository;

    /**
     * Process merchant payment with IBAN, CVV, and expiry date.
     * Uses factory to delegate to appropriate payment flow (Credit/Debit).
     */
    @Transactional
    public CardTransactionResponse processMerchantPayment(
            String cardToken,
            String merchantId,
            String iban,
            String cvv,
            String expiryDate,
            BigDecimal amount,
            String currency,
            String idempotencyKey) throws Exception {

        log.info("Starting merchant payment processing for merchant: {}", merchantId);

        // Retrieve or cache card
        Card card = cardAccessService.getCardByToken(cardToken);
        if (card == null) {
            throw new IllegalArgumentException("Card not found for token: " + cardToken);
        }

        // Create appropriate payment flow using factory
        PaymentFlow paymentFlow = paymentFlowFactory.createPaymentFlow(card);

        log.info("Using payment flow: {}", paymentFlow.getFlowType());

        // Delegate to payment flow
        return paymentFlow.processPayment(
                card,
                merchantId,
                iban,
                cvv,
                expiryDate,
                amount,
                currency,
                idempotencyKey
        );
    }
}
