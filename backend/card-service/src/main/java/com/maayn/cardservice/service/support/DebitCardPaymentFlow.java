package com.maayn.cardservice.service.support;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.repository.CardTransactionRepository;
import com.maayn.cardservice.entity.CardTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.card.CardTransactionResponse;
import maayn.veld.generated.models.card.CardStatus;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionResponse;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Debit Card Payment Flow Implementation (SOLID: Single Responsibility).
 * Handles debit card payment processing with immediate fund deduction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DebitCardPaymentFlow implements PaymentFlow {

    private final TransactionGateway transactionGateway;
    private final CardTransactionFactory cardTransactionFactory;
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

        log.info("Processing DEBIT card payment for merchant: {}", merchantId);

        // Validate card state (status, blocking)
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Card is not active. Current status: " + card.getStatus());
        }
        if (card.getBlockedAt() != null) {
            throw new IllegalArgumentException("Card is blocked: " + card.getBlockReason());
        }

        // Validate optional merchant payment details if provided
        if (iban != null || cvv != null || expiryDate != null) {
            merchantPaymentValidator.validateMerchantPayment(iban, cvv, expiryDate, card);
        }

        // Process through transaction gateway (funds deducted immediately)
        TransactionResponse transferResult = transactionGateway.transfer(
                card.getAccountId(),
                getMerchantVaultAccount(),
                amount,
                currency,
                idempotencyKey,
                TransactionType.CARD_PAYMENT
        );

        // Create and persist debit card transaction
        CardTransaction transaction = cardTransactionFactory.createPurchase(
                card,
                UUID.fromString(merchantId),
                idempotencyKey,
                transferResult.getReferenceNumber(),
                amount,
                currency
        );

        CardTransaction saved = cardTransactionRepository.save(transaction);
        log.info("DEBIT payment processed successfully: {}", saved.getId());

        return CardMapper.toTransactionResponse(saved);
    }

    @Override
    public PaymentFlowType getFlowType() {
        return PaymentFlowType.DEBIT_CARD;
    }

    private String getMerchantVaultAccount() {
        // In production, this would be dynamic based on merchant routing
        return "99999999-9999-9999-9999-999999999998";
    }
}
