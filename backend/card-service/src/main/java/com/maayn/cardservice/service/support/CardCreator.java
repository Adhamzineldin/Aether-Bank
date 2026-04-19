package com.maayn.cardservice.service.support;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CreateCardRequest;
import com.maayn.cardservice.exception.CreateCardException;

/**
 * Strategy interface for creating different card types (DEBIT, CREDIT, PREPAID).
 * 
 * Each implementation handles type-specific initialization:
 * - DebitCardCreator: No credit details, direct balance
 * - CreditCardCreator: CreditCardDetailsEntity setup, credit limits
 * - PrepaidCardCreator: No credit details, prepaid balance
 */
public interface CardCreator {

    /**
     * Create a new card entity of the specific type.
     * Implementations must:
     * - Set basic card metadata (token, last 4 digits, expiry)
     * - Initialize type-specific details
     * - Validate all required fields
     * 
     * @param request the card creation request
     * @return a fully initialized Card entity ready for persistence
     * @throws CreateCardException if creation fails validation
     */
    Card createCard(CreateCardRequest request) throws CreateCardException;
}
