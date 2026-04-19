package com.maayn.cardservice.service.support;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CreateCardRequest;
import com.maayn.cardservice.entity.CreditCardDetailsEntity;
import com.maayn.cardservice.exception.CreateCardException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Card creation strategy for CREDIT cards.
 * 
 * Credit cards characteristics:
 * - CreditCardDetailsEntity stores credit limit, available credit, balance
 * - Requires interest rate configuration
 * - Billing cycle setup for statement generation
 * - Credit limit enforcement for purchases
 * 
 * This implementation creates a complete credit card setup including
 * the credit details entity with proper initialization.
 */
@Component
public class CreditCardCreator extends AbstractCardCreator {

    private static final BigDecimal MINIMUM_PAYMENT_PERCENT = new BigDecimal("0.05");
    private static final Integer DEFAULT_BILLING_CYCLE_DAY = 1;

    /**
     * Setup credit card specific details.
     * Creates a CreditCardDetailsEntity with:
     * - Credit limit from request
     * - Available credit equals credit limit (new card)
     * - Current balance = 0 (new card)
     * - Minimum payment = 5% of credit limit
     * - Interest rate from request
     * - Billing cycle day from request
     * 
     * @param card the card being created
     * @param request the creation request
     * @throws CreateCardException if setup fails validation
     */
    @Override
    protected void setupTypeSpecificDetails(Card card, CreateCardRequest request) throws CreateCardException {
        validateCreditRequest(request);
        
        CreditCardDetailsEntity creditDetails = new CreditCardDetailsEntity();
        BigDecimal creditLimit = request.getInitialBalance();
        
        creditDetails.setCard(card);
        creditDetails.setCreditLimit(creditLimit);
        creditDetails.setAvailableCredit(creditLimit);
        creditDetails.setCurrentBalance(BigDecimal.ZERO);
        creditDetails.setMinimumPayment(creditLimit.multiply(MINIMUM_PAYMENT_PERCENT));
        creditDetails.setAnnualInterestRate(getInterestRate(request));
        creditDetails.setBillingCycleDay(validateBillingCycleDay(request.getBillingCycleDay()));
        creditDetails.setLastStatementDate(LocalDate.now());
        creditDetails.setPaymentDueDate(calculatePaymentDueDate(creditDetails.getBillingCycleDay()));
        
        card.setCreditDetails(creditDetails);
    }

    /**
     * Validate credit card specific request fields.
     * 
     * @param request the creation request
     * @throws CreateCardException if validation fails
     */
    private void validateCreditRequest(CreateCardRequest request) throws CreateCardException {
        if (request.getInitialBalance().signum() <= 0) {
            throw new CreateCardException("Credit limit must be positive");
        }
    }

    /**
     * Get interest rate from request, defaulting to 0 if not provided.
     * 
     * @param request the creation request
     * @return annual interest rate (default: 0)
     */
    private BigDecimal getInterestRate(CreateCardRequest request) {
        return request.getAnnualInterestRate() != null 
            ? request.getAnnualInterestRate() 
            : BigDecimal.ZERO;
    }

    /**
     * Calculate payment due date based on billing cycle day and last statement date.
     * Due date is typically 20-25 days after statement date.
     * For now: uses billing cycle day + 21 days.
     * 
     * @param billingCycleDay the day of month billing occurs
     * @return the calculated payment due date
     */
    private LocalDate calculatePaymentDueDate(Integer billingCycleDay) {
        return LocalDate.now().plusDays(21);
    }
}
