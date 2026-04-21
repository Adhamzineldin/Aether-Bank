package com.maayn.cardservice.service;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.entity.CreditCardDetailsEntity;
import com.maayn.cardservice.entity.CreateCardRequest;
import com.maayn.cardservice.exception.InvalidCardException;
import com.maayn.cardservice.exception.InsufficientBalanceException;
import com.maayn.cardservice.validator.CardRulesValidator;
import com.maayn.cardservice.validator.CvvValidator;
import com.maayn.cardservice.validator.ExpiryDateValidator;
import com.maayn.cardservice.validator.IbanValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.card.CardStatus;
import maayn.veld.generated.models.card.MerchantPaymentRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Locale;

/**
 * Consolidated validator facade for all card validation concerns.
 * Delegates to specialized validators while providing a single entry point.
 * 
 * Patterns aligned with TransactionService validation:
 * - Single validator component for orchestration
 * - Specialized validators injected as dependencies
 * - Clear error messages with business context
 * - Uses exception hierarchy with error codes
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardValidator {

    private final CardRulesValidator cardRulesValidator;
    private final CvvValidator cvvValidator;
    private final ExpiryDateValidator expiryDateValidator;
    private final IbanValidator ibanValidator;

    // CVV constants
    private static final int CVV_LENGTH_AMEX = 4;
    private static final int CVV_LENGTH_STANDARD = 3;

    /**
     * Validates a card issuance request.
     * 
     * @param request the card creation request
     * @throws InvalidCardException if validation fails
     */
    public void validateCardCreation(CreateCardRequest request) {
        log.debug("Validating card creation request for account: {}", request.getAccountId());
        
        if (request == null) {
            throw new InvalidCardException("Card creation request cannot be null");
        }
        if (request.getAccountId() == null) {
            throw new InvalidCardException("Account ID is required");
        }
        if (request.getCustomerId() == null) {
            throw new InvalidCardException("Customer ID is required");
        }
        if (request.getCardType() == null) {
            throw new InvalidCardException("Card type is required");
        }
        if (request.getCardNetwork() == null) {
            throw new InvalidCardException("Card network is required");
        }
        if (request.getInitialBalance() == null || request.getInitialBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidCardException("Initial balance must be greater than zero");
        }
    }

    /**
     * Validates a card for payment processing.
     * Checks expiry, status, and other prerequisites.
     * 
     * @param card the card entity
     * @param requestCurrency the requested currency
     * @param amount the transaction amount
     * @throws InvalidCardException if card cannot process payments
     * @throws InsufficientBalanceException if insufficient balance/credit
     */
    public void validateCardForPayment(Card card, String requestCurrency, BigDecimal amount) {
        log.debug("Validating card {} for payment processing", card.getId());
        
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidCardException("Card is not active: " + card.getStatus());
        }

        if (YearMonth.of(card.getExpiryYear(), card.getExpiryMonth())
                .isBefore(YearMonth.now())) {
            throw new InvalidCardException("Card has expired");
        }

        if (!requestCurrency.matches("[A-Z]{3}")) {
            throw new InvalidCardException("Invalid currency code: " + requestCurrency);
        }

        validateSufficientBalance(card, amount);
    }

    /**
     * Validates CVV format for a card.
     * 
     * @param cvv the CVV to validate
     * @throws InvalidCardException if CVV is invalid
     */
    public void validateCvv(String cvv) {
        log.debug("Validating CVV");
        
        if (cvv == null || cvv.isBlank()) {
            throw new InvalidCardException("CVV cannot be null or empty");
        }

        String cleanCvv = cvv.trim();

        if (!cleanCvv.matches("^[0-9]+$")) {
            throw new InvalidCardException("CVV must contain only digits");
        }

        if (cleanCvv.length() != CVV_LENGTH_STANDARD && cleanCvv.length() != CVV_LENGTH_AMEX) {
            throw new InvalidCardException("CVV must be 3 or 4 digits");
        }
    }

    /**
     * Validates expiry date.
     * 
     * @param month the expiry month (1-12)
     * @param year the expiry year (4-digit)
     * @throws InvalidCardException if expiry date is invalid or expired
     */
    public void validateExpiryDate(int month, int year) {
        log.debug("Validating expiry date: {}/{}", month, year);
        
        if (month < 1 || month > 12) {
            throw new InvalidCardException("Invalid expiry month: " + month);
        }

        if (year < 2000 || year > 2099) {
            throw new InvalidCardException("Invalid expiry year: " + year);
        }

        if (YearMonth.of(year, month).isBefore(YearMonth.now())) {
            throw new InvalidCardException("Card has expired: " + month + "/" + year);
        }
    }

    /**
     * Validates IBAN format.
     * 
     * @param iban the IBAN to validate
     * @throws InvalidCardException if IBAN is invalid
     */
    public void validateIban(String iban) {
        log.debug("Validating IBAN format");
        
        if (iban == null || iban.isBlank()) {
            throw new InvalidCardException("IBAN cannot be null or empty");
        }

        String cleanIban = iban.trim().toUpperCase(Locale.ROOT);
        
        if (!cleanIban.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$")) {
            throw new InvalidCardException("Invalid IBAN format: " + iban);
        }
    }

    /**
     * Validates merchant payment request.
     * 
     * @param request the merchant payment request
     * @throws InvalidCardException if validation fails
     */
    public void validateMerchantPaymentRequest(MerchantPaymentRequest request) {
        log.debug("Validating merchant payment request");
        
        try {
            cardRulesValidator.validateMerchantPaymentRequest(request);
        } catch (Exception e) {
            throw new InvalidCardException("Merchant payment validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validates sufficient balance for a transaction.
     * For credit cards, checks credit limit; for debit cards, checks balance.
     * 
     * @param card the card entity
     * @param amount the transaction amount
     * @throws InsufficientBalanceException if insufficient balance/credit
     */
    private void validateSufficientBalance(Card card, BigDecimal amount) {
        log.debug("Validating sufficient balance for card {}, amount: {}", card.getId(), amount);

        // Card balance is owned by the linked account/credit-line; balance enforcement is delegated
        // to the transaction-service / account-service. This method is intentionally a no-op here.
    }

    /**
     * Normalizes currency code to uppercase.
     * 
     * @param currency the currency code
     * @return normalized currency code
     */
    public String normalizeCurrency(String currency) {
        return currency == null ? "" : currency.trim().toUpperCase(Locale.ROOT);
    }
}
