package com.maayn.cardservice.service.support;

import com.maayn.cardservice.entity.CreateCardRequest;
import com.maayn.cardservice.exception.CreateCardException;
import maayn.veld.generated.models.card.CardType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Validation rules for card creation requests.
 * 
 * Validates:
 * - Required fields are present
 * - Values are within acceptable ranges
 * - Card-type-specific constraints
 * 
 * This keeps validation logic separate from creation logic,
 * making rules easier to test and modify.
 */
@Component
public class CardIssuanceValidator {

    private static final BigDecimal MIN_BALANCE = new BigDecimal("0.01");
    private static final BigDecimal MAX_BALANCE = new BigDecimal("999999.99");
    private static final BigDecimal MIN_INTEREST_RATE = BigDecimal.ZERO;
    private static final BigDecimal MAX_INTEREST_RATE = new BigDecimal("1.0");

    /**
     * Validate a card creation request comprehensively.
     * 
     * @param request the request to validate
     * @throws CreateCardException if validation fails
     */
    public void validate(CreateCardRequest request) throws CreateCardException {
        if (request == null) {
            throw new CreateCardException("Card creation request cannot be null");
        }

        validateRequiredFields(request);
        validateBalances(request);
        validateCreditCardFields(request);
    }

    /**
     * Validate that all required fields are present and non-null.
     * 
     * @param request the request to validate
     * @throws CreateCardException if required fields are missing
     */
    private void validateRequiredFields(CreateCardRequest request) throws CreateCardException {
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
        if (request.getInitialBalance() == null) {
            throw new CreateCardException("Initial balance is required");
        }
    }

    /**
     * Validate initial balance/credit limit values.
     * - Must be positive
     * - Must be within reasonable range
     * 
     * @param request the request to validate
     * @throws CreateCardException if balance is invalid
     */
    private void validateBalances(CreateCardRequest request) throws CreateCardException {
        BigDecimal balance = request.getInitialBalance();

        if (balance.compareTo(MIN_BALANCE) < 0) {
            throw new CreateCardException(
                String.format("Initial balance must be at least %s", MIN_BALANCE)
            );
        }

        if (balance.compareTo(MAX_BALANCE) > 0) {
            throw new CreateCardException(
                String.format("Initial balance cannot exceed %s", MAX_BALANCE)
            );
        }
    }

    /**
     * Validate credit-card-specific fields (only when card type is CREDIT).
     * 
     * @param request the request to validate
     * @throws CreateCardException if credit card fields are invalid
     */
    private void validateCreditCardFields(CreateCardRequest request) throws CreateCardException {
        if (request.getCardType() != CardType.CREDIT) {
            return; // Not a credit card, skip credit-specific validation
        }

        if (request.getAnnualInterestRate() != null) {
            BigDecimal rate = request.getAnnualInterestRate();
            if (rate.compareTo(MIN_INTEREST_RATE) < 0 || rate.compareTo(MAX_INTEREST_RATE) > 0) {
                throw new CreateCardException(
                    String.format("Annual interest rate must be between %s and %s", 
                        MIN_INTEREST_RATE, MAX_INTEREST_RATE)
                );
            }
        }

        if (request.getBillingCycleDay() != null) {
            Integer day = request.getBillingCycleDay();
            if (day < 1 || day > 28) {
                throw new CreateCardException(
                    "Billing cycle day must be between 1 and 28 to avoid month-end issues"
                );
            }
        }
    }
}
