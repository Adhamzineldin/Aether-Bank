package com.maayn.cardservice.service.support;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CreateCardRequest;
import com.maayn.cardservice.exception.CreateCardException;
import maayn.veld.generated.models.card.CardStatus;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;
import java.util.UUID;

/**
 * Abstract base class for card creation strategies.
 * Handles common card setup logic shared across all card types.
 * 
 * Subclasses implement type-specific details:
 * - DebitCardCreator: Balance setup only
 * - CreditCardCreator: Credit details + interest rates
 */
public abstract class AbstractCardCreator implements CardCreator {

    private static final int CARD_VALIDITY_YEARS = 5;
    private static final int LAST_FOUR_LENGTH = 4;

    /**
     * Template method defining card creation flow.
     * 1. Validate request
     * 2. Create base card
     * 3. Set common metadata
     * 4. Let subclasses add type-specific details
     * 5. Return complete card
     */
    @Override
    public final Card createCard(CreateCardRequest request) throws CreateCardException {
        validateRequest(request);
        
        Card card = new Card();
        setBasicMetadata(card, request);
        setupTypeSpecificDetails(card, request);
        
        return card;
    }

    /**
     * Validate the creation request.
     * Checks for null fields and invalid values.
     * 
     * @param request the request to validate
     * @throws CreateCardException if validation fails
     */
    protected void validateRequest(CreateCardRequest request) throws CreateCardException {
        if (request == null) {
            throw new CreateCardException("Create card request cannot be null");
        }
        if (request.getAccountId() == null) {
            throw new CreateCardException("Account ID is required");
        }
        if (request.getCustomerId() == null) {
            throw new CreateCardException("Customer ID is required");
        }
        if (request.getCardType() == null) {
            throw new CreateCardException("Card type is required");
        }
        if (request.getCardNetwork() == null) {
            throw new CreateCardException("Card network is required");
        }
        if (request.getInitialBalance() == null || request.getInitialBalance().signum() <= 0) {
            throw new CreateCardException("Initial balance must be positive");
        }
    }

    /**
     * Set card metadata common to all types.
     * - Account and customer IDs
     * - Card token (unique)
     * - Last 4 digits
     * - Card type and network
     * - Status: ACTIVE
     * - Expiry: 5 years from now
     * 
     * @param card the card to populate
     * @param request the creation request
     */
    private void setBasicMetadata(Card card, CreateCardRequest request) {
        card.setAccountId(request.getAccountId());
        card.setCustomerId(request.getCustomerId());
        card.setCardToken(generateCardToken());
        card.setLastFourDigits(generateLastFourDigits());
        card.setCardType(request.getCardType());
        card.setCardNetwork(request.getCardNetwork());
        card.setStatus(CardStatus.ACTIVE);
        
        YearMonth expiry = YearMonth.now().plusYears(CARD_VALIDITY_YEARS);
        card.setExpiryMonth(expiry.getMonthValue());
        card.setExpiryYear(expiry.getYear());
    }

    /**
     * Hook for subclasses to add type-specific details.
     * DebitCardCreator: Skip (no credit details)
     * CreditCardCreator: Create CreditCardDetailsEntity with limits and rates
     * 
     * @param card the card being created
     * @param request the creation request
     * @throws CreateCardException if type-specific setup fails
     */
    protected abstract void setupTypeSpecificDetails(Card card, CreateCardRequest request) throws CreateCardException;

    /**
     * Generate a unique card token for merchants.
     * Format: Random 32-char alphanumeric.
     * 
     * @return the generated token
     */
    protected String generateCardToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generate last 4 digits of card for display.
     * Format: Random 4 digits.
     * 
     * @return last 4 digits as string
     */
    protected String generateLastFourDigits() {
        int random = (int) (Math.random() * 10000);
        return String.format(Locale.ROOT, "%04d", random);
    }

    /**
     * Get default billing cycle day.
     * Can be overridden by subclasses.
     * 
     * @return day of month for billing cycle (default: 1st)
     */
    protected Integer getDefaultBillingCycleDay() {
        return 1;
    }

    /**
     * Validate and get billing cycle day from request.
     * Must be between 1-28 to avoid month-end issues.
     * 
     * @param billingCycleDay the requested day, or null for default
     * @return validated day (1-28)
     * @throws CreateCardException if day is invalid
     */
    protected Integer validateBillingCycleDay(Integer billingCycleDay) throws CreateCardException {
        if (billingCycleDay == null) {
            return getDefaultBillingCycleDay();
        }
        if (billingCycleDay < 1 || billingCycleDay > 28) {
            throw new CreateCardException("Billing cycle day must be between 1 and 28");
        }
        return billingCycleDay;
    }
}
