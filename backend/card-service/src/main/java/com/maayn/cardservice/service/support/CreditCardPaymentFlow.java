package com.maayn.cardservice.service.support;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.repository.CardTransactionRepository;
import com.maayn.cardservice.entity.CardTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.card.CardTransactionResponse;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionResponse;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Credit Card Payment Flow Implementation (SOLID: Single Responsibility).
 * Handles credit card payment processing with merchant IBAN routing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditCardPaymentFlow implements PaymentFlow {

    private final TransactionGateway transactionGateway;
    private final CardTransactionFactory cardTransactionFactory;
    private final CreditBalanceService creditBalanceService;
    private final CardTransactionRepository cardTransactionRepository;
    private final MerchantPaymentValidator merchantPaymentValidator;

    @Override
    public CardTransactionResponse processPayment(
            Card card,
            String merchantId,
            String iban,
            String cvv,
            String expiryDate,
            BigDecimal amount,
            String currency,
            String idempotencyKey) throws Exception {

        log.info("Processing CREDIT card payment for merchant: {} with IBAN: {}", merchantId, maskIban(iban));

        // Validate merchant payment details
        merchantPaymentValidator.validateMerchantPayment(iban, cvv, expiryDate, card);

        // Process through transaction gateway
        TransactionResponse transferResult = transactionGateway.transfer(
                card.getAccountId(),
                getMerchantVaultAccount(),
                amount,
                currency,
                idempotencyKey,
                TransactionType.CARD_PAYMENT
        );

        // Create and persist credit card transaction
        CardTransaction transaction = cardTransactionFactory.createPurchase(
                card,
                UUID.fromString(merchantId),
                idempotencyKey,
                transferResult.getReferenceNumber(),
                amount,
                currency
        );

        // Apply charge to credit balance
        creditBalanceService.applyCharge(card, amount);

        CardTransaction saved = cardTransactionRepository.save(transaction);
        log.info("CREDIT payment processed successfully: {}", saved.getId());

        return CardMapper.toTransactionResponse(saved);
    }

    @Override
    public PaymentFlowType getFlowType() {
        return PaymentFlowType.CREDIT_CARD;
    }

    private String getMerchantVaultAccount() {
        // In production, this would be dynamic based on merchant routing
        return "99999999-9999-9999-9999-999999999998";
    }

    private String maskIban(String iban) {
        if (iban == null || iban.length() < 8) return iban;
        return iban.substring(0, 4) + "****" + iban.substring(iban.length() - 4);
    }
}
