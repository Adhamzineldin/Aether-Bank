package com.maayn.cardservice.service.support;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CreateCardRequest;
import com.maayn.cardservice.exception.CreateCardException;
import org.springframework.stereotype.Component;

/**
 * Card creation strategy for DEBIT cards.
 * 
 * Debit cards characteristics:
 * - No credit details entity (balance comes from checking/savings account)
 * - Direct balance access (no credit limit concept)
 * - No interest rates or billing cycles
 * - Balance tracked in core banking system, not here
 * 
 * This implementation focuses on minimal setup since balance management
 * is handled by the core account service.
 */
@Component
public class DebitCardCreator extends AbstractCardCreator {

    /**
     * Setup debit card specific details.
     * For debit cards, we don't create a CreditCardDetailsEntity.
     * The card references an account in the core banking system for balance.
     * 
     * @param card the card being created
     * @param request the creation request
     * @throws CreateCardException if setup fails
     */
    @Override
    protected void setupTypeSpecificDetails(Card card, CreateCardRequest request) throws CreateCardException {
        // Debit cards don't need CreditCardDetailsEntity
        // Balance is managed by the linked account in core banking
        // No setup required beyond base metadata
        validateInitialBalance(request.getInitialBalance());
    }

    /**
     * Validate that initial balance is positive.
     * For debit cards, this represents the opening balance in the linked account.
     * 
     * @param initialBalance the balance to validate
     * @throws CreateCardException if balance is invalid
     */
    private void validateInitialBalance(java.math.BigDecimal initialBalance) throws CreateCardException {
        if (initialBalance.signum() <= 0) {
            throw new CreateCardException("Debit card initial balance must be positive");
        }
    }
}
