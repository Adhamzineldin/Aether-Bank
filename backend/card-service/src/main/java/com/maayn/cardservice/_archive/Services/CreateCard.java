package com.maayn.cardservice.Services;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CreditCardDetailsEntity;
import com.maayn.cardservice.repository.CardRepository;
import com.maayn.cardservice.repository.CreditCardDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.card.CardNetwork;
import maayn.veld.generated.models.card.CardStatus;
import maayn.veld.generated.models.card.CardType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service responsible for creating and issuing new cards.
 * Handles both debit and credit card creation with proper validation and persistence.
 * 
 * Features:
 * - Professional dependency injection with @RequiredArgsConstructor
 * - Structured logging with @Slf4j
 * - Type-specific card creation (debit/credit)
 * - Credit limit management for credit cards
 * - Transactional integrity
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateCard {

    private final CardRepository cardRepository;
    private final CreditCardDetailsRepository creditCardDetailsRepository;

    /**
     * Creates a debit card for the specified account and customer.
     * 
     * @param accountId The account UUID
     * @param customerId The customer UUID
     * @param cardNetwork The card network (VISA, MASTERCARD, etc.)
     * @return The created debit Card entity
     * @throws IllegalArgumentException if parameters are invalid
     */
    @Transactional
    public Card createDebitCard(
            UUID accountId,
            UUID customerId,
            CardNetwork cardNetwork
    ) {
        log.info("Creating debit card for account: {}, customer: {}, network: {}", 
            accountId, customerId, cardNetwork);

        validateCardCreationParameters(accountId, customerId, cardNetwork);

        Card card = buildBaseCard(accountId, customerId, CardType.DEBIT, cardNetwork);
        
        try {
            Card savedCard = cardRepository.save(card);
            log.info("Debit card created successfully with ID: {}, token: {}", 
                savedCard.getId(), maskCardToken(savedCard.getCardToken()));
            return savedCard;
        } catch (Exception e) {
            log.error("Failed to create debit card for account: {}", accountId, e);
            throw new RuntimeException("Card creation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a credit card for the specified account and customer.
     * Automatically initializes credit limit and balance tracking.
     * 
     * @param accountId The account UUID
     * @param customerId The customer UUID
     * @param cardNetwork The card network (VISA, MASTERCARD, etc.)
     * @param creditLimit The credit limit to assign
     * @param annualInterestRate The annual interest rate
     * @return The created credit Card entity with credit details
     * @throws IllegalArgumentException if parameters are invalid
     */
    @Transactional
    public Card createCreditCard(
            UUID accountId,
            UUID customerId,
            CardNetwork cardNetwork,
            BigDecimal creditLimit,
            BigDecimal annualInterestRate
    ) {
        log.info("Creating credit card for account: {}, customer: {}, network: {}, limit: {}", 
            accountId, customerId, cardNetwork, creditLimit);

        validateCardCreationParameters(accountId, customerId, cardNetwork);
        validateCreditCardParameters(creditLimit, annualInterestRate);

        // Create base card
        Card card = buildBaseCard(accountId, customerId, CardType.CREDIT, cardNetwork);
        Card savedCard = cardRepository.save(card);
        
        try {
            // Initialize credit details
            CreditCardDetailsEntity creditDetails = buildCreditCardDetails(
                savedCard, creditLimit, annualInterestRate
            );
            CreditCardDetailsEntity savedCreditDetails = creditCardDetailsRepository.save(creditDetails);
            savedCard.setCreditDetails(savedCreditDetails);
            
            log.info("Credit card created successfully with ID: {}, token: {}, limit: {}", 
                savedCard.getId(), maskCardToken(savedCard.getCardToken()), creditLimit);
            return savedCard;
        } catch (Exception e) {
            log.error("Failed to create credit card details for card: {}", savedCard.getId(), e);
            // Clean up: delete the card if credit details creation fails
            cardRepository.deleteById(savedCard.getId());
            throw new RuntimeException("Credit card details initialization failed: " + e.getMessage(), e);
        }
    }

    /**
     * Builds a base Card entity with common properties.
     * 
     * @param accountId The account UUID
     * @param customerId The customer UUID
     * @param cardType The type of card (DEBIT or CREDIT)
     * @param cardNetwork The card network
     * @return A new Card entity (not yet persisted)
     */
    private Card buildBaseCard(
            UUID accountId,
            UUID customerId,
            CardType cardType,
            CardNetwork cardNetwork
    ) {
        Card card = new Card();
        card.setAccountId(accountId);
        card.setCustomerId(customerId);
        card.setCardType(cardType);
        card.setCardNetwork(cardNetwork);
        card.setStatus(CardStatus.ACTIVE);
        
        // Generate card token (in production, this would come from payment processor)
        card.setCardToken(generateCardToken());
        
        // Generate last four digits
        card.setLastFourDigits(generateLastFourDigits());
        
        // Set expiry (5 years from now)
        LocalDate expiryDate = LocalDate.now().plusYears(5);
        card.setExpiryMonth(expiryDate.getMonthValue());
        card.setExpiryYear(expiryDate.getYear());
        
        return card;
    }

    /**
     * Builds credit card details with initial balance and credit limit.
     * 
     * @param card The card entity to associate with
     * @param creditLimit The credit limit
     * @param annualInterestRate The annual interest rate
     * @return A new CreditCardDetailsEntity (not yet persisted)
     */
    private CreditCardDetailsEntity buildCreditCardDetails(
            Card card,
            BigDecimal creditLimit,
            BigDecimal annualInterestRate
    ) {
        CreditCardDetailsEntity details = new CreditCardDetailsEntity();
        details.setCard(card);
        details.setCreditLimit(creditLimit);
        details.setAvailableCredit(creditLimit);
        details.setCurrentBalance(BigDecimal.ZERO);
        details.setMinimumPayment(creditLimit.multiply(new BigDecimal("0.02"))); // 2% minimum
        details.setAnnualInterestRate(annualInterestRate);
        details.setBillingCycleDay(1);
        details.setPaymentDueDate(LocalDate.now().plusMonths(1));
        
        return details;
    }

    /**
     * Validates basic card creation parameters.
     * 
     * @param accountId The account UUID
     * @param customerId The customer UUID
     * @param cardNetwork The card network
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    private void validateCardCreationParameters(
            UUID accountId,
            UUID customerId,
            CardNetwork cardNetwork
    ) {
        if (accountId == null) {
            log.warn("Card creation attempted with null accountId");
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        if (customerId == null) {
            log.warn("Card creation attempted with null customerId");
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (cardNetwork == null) {
            log.warn("Card creation attempted with null cardNetwork");
            throw new IllegalArgumentException("Card network cannot be null");
        }
    }

    /**
     * Validates credit card specific parameters.
     * 
     * @param creditLimit The credit limit
     * @param annualInterestRate The annual interest rate
     * @throws IllegalArgumentException if parameters are invalid
     */
    private void validateCreditCardParameters(
            BigDecimal creditLimit,
            BigDecimal annualInterestRate
    ) {
        if (creditLimit == null || creditLimit.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Credit card creation attempted with invalid credit limit: {}", creditLimit);
            throw new IllegalArgumentException("Credit limit must be positive");
        }
        if (annualInterestRate == null || annualInterestRate.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Credit card creation attempted with invalid interest rate: {}", annualInterestRate);
            throw new IllegalArgumentException("Annual interest rate cannot be negative");
        }
    }

    /**
     * Generates a unique card token.
     * In production, this would integrate with a payment processor.
     * 
     * @return A unique card token
     */
    private String generateCardToken() {
        return "card_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generates random last four digits for display purposes.
     * 
     * @return A 4-digit string
     */
    private String generateLastFourDigits() {
        int random = (int) (Math.random() * 10000);
        return String.format("%04d", random);
    }

    /**
     * Masks card token for safe logging (shows only last 4 characters).
     * 
     * @param token The full card token
     * @return Masked token for safe display
     */
    private String maskCardToken(String token) {
        if (token == null || token.length() < 4) {
            return "****";
        }
        return "****" + token.substring(token.length() - 4);
    }
}
