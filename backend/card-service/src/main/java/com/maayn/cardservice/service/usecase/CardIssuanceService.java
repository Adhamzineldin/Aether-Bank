package com.maayn.cardservice.service.usecase;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CreateCardRequest;
import com.maayn.cardservice.exception.CreateCardException;
import com.maayn.cardservice.repository.CardRepository;
import com.maayn.cardservice.repository.CreditCardDetailsRepository;
import com.maayn.cardservice.service.support.CardCreator;
import com.maayn.cardservice.service.support.CardCreatorFactory;
import com.maayn.cardservice.service.support.CardIssuanceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use-case service for card issuance.
 * 
 * Orchestrates the full card creation flow:
 * 1. Validate request
 * 2. Get appropriate creator strategy
 * 3. Create card entity
 * 4. Persist to database
 * 5. Return created card
 * 
 * Handles both CREDIT and DEBIT card issuance with type-specific logic.
 */
@Service
@RequiredArgsConstructor
public class CardIssuanceService {

    private final CardIssuanceValidator validator;
    private final CardCreatorFactory creatorFactory;
    private final CardRepository cardRepository;
    private final CreditCardDetailsRepository creditCardDetailsRepository;

    /**
     * Issue a new card of the specified type.
     * 
     * Flow:
     * 1. Validate the creation request
     * 2. Get the appropriate creator (CREDIT vs DEBIT strategy)
     * 3. Create the card entity with type-specific setup
     * 4. Persist the card and any related details
     * 5. Return the created card
     * 
     * @param request the card creation request
     * @return the newly created and persisted Card
     * @throws CreateCardException if validation fails or creation encounters errors
     */
    @Transactional
    public Card issueCard(CreateCardRequest request) throws CreateCardException {
        validator.validate(request);
        
        CardCreator creator = creatorFactory.getCreator(request.getCardType());
        Card card = creator.createCard(request);
        
        return persistCard(card);
    }

    /**
     * Persist the card and any associated details (credit details).
     * Uses cascading to handle related entities automatically.
     * 
     * @param card the card to persist
     * @return the persisted card with ID populated
     * @throws CreateCardException if persistence fails
     */
    private Card persistCard(Card card) throws CreateCardException {
        try {
            return cardRepository.save(card);
        } catch (Exception ex) {
            throw new CreateCardException("Failed to persist card: " + ex.getMessage(), ex);
        }
    }
}
