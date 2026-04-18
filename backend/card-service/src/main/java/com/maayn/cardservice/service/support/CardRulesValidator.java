package com.maayn.cardservice.service.support;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.entity.CreditCardDetailsEntity;
import maayn.veld.generated.errors.CardErrors;
import maayn.veld.generated.errors.ProcessMerchantPaymentException;
import maayn.veld.generated.errors.RefundTransactionException;
import maayn.veld.generated.errors.VoidTransactionException;
import maayn.veld.generated.models.card.CardStatus;
import maayn.veld.generated.models.card.CardTransactionStatus;
import maayn.veld.generated.models.card.CardTransactionType;
import maayn.veld.generated.models.card.CardType;
import maayn.veld.generated.models.card.MerchantPaymentRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Locale;

@Component
/**
 * Encapsulates the card domain rules used by payment, refund, and void flows.
 * Keeping them here avoids scattering validation logic across multiple services.
 */
public class CardRulesValidator {

    public void validateMerchantPaymentRequest(MerchantPaymentRequest input) throws ProcessMerchantPaymentException {
        if (input == null || !hasText(input.getCardToken())) {
            throw CardErrors.processMerchantPayment.invalidCardToken("Card token is required");
        }
        if (input.getAmount() == null || input.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw CardErrors.processMerchantPayment.invalidAmount("Amount must be greater than zero");
        }
        if (!hasText(input.getCurrency()) || normalizeCurrency(input.getCurrency()).length() != 3) {
            throw CardErrors.processMerchantPayment.invalidCurrency("Currency must be a 3-letter ISO code");
        }
    }

    public void validateCardForPayment(Card card, String requestCurrency, BigDecimal amount) throws ProcessMerchantPaymentException {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw CardErrors.processMerchantPayment.cardNotActive("Card is not active");
        }
        if (YearMonth.of(card.getExpiryYear(), card.getExpiryMonth()).isBefore(YearMonth.now())) {
            throw CardErrors.processMerchantPayment.cardExpired("Card has expired");
        }
        if (!normalizeCurrency(requestCurrency).matches("[A-Z]{3}")) {
            throw CardErrors.processMerchantPayment.invalidCurrency("Currency must be a 3-letter ISO code");
        }
        if (card.getCardType() == CardType.CREDIT) {
            // Debit/prepaid cards rely on the transaction service balance check; credit cards also enforce card credit limits here.
            CreditCardDetailsEntity creditDetails = card.getCreditDetails();
            if (creditDetails == null) {
                throw CardErrors.processMerchantPayment.insufficientCredit("Credit card details are missing");
            }
            if (creditDetails.getAvailableCredit().compareTo(amount) < 0) {
                throw CardErrors.processMerchantPayment.insufficientCredit("Available credit is lower than requested amount");
            }
        }
    }

    public BigDecimal validateRefund(CardTransaction original, BigDecimal requestedAmount) throws RefundTransactionException {
        if (original.getType() != CardTransactionType.PURCHASE || original.getStatus() != CardTransactionStatus.APPROVED) {
            throw CardErrors.refundTransaction.refundNotAllowed("Only approved purchase transactions can be refunded");
        }

        // If the caller omits an amount, the refund defaults to the entire original purchase value.
        BigDecimal refundAmount = requestedAmount != null ? requestedAmount : original.getAmount();
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw CardErrors.refundTransaction.invalidAmount("Refund amount must be greater than zero");
        }
        if (refundAmount.compareTo(original.getAmount()) != 0) {
            throw CardErrors.refundTransaction.refundNotAllowed("Partial refunds are not supported yet");
        }
        return refundAmount;
    }

    public void validateVoid(CardTransaction original) throws VoidTransactionException {
        if (original.getType() != CardTransactionType.PURCHASE || original.getStatus() != CardTransactionStatus.APPROVED) {
            throw CardErrors.voidTransaction.voidNotAllowed("Only approved purchase transactions can be voided");
        }
        // Voids are only allowed in a short post-authorization window; after that the transaction must be refunded.
        if (original.getProcessedAt() == null || original.getProcessedAt().isBefore(LocalDateTime.now().minusMinutes(15))) {
            throw CardErrors.voidTransaction.voidNotAllowed("Void window has expired");
        }
    }

    public String normalizeCurrency(String currency) {
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
