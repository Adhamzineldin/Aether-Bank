package com.maayn.cardservice.service.support;

import com.maayn.cardservice.exception.CreateCardException;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.models.card.CardType;
import org.springframework.stereotype.Component;

/**
 * Factory for obtaining the appropriate CardCreator strategy based on card type.
 * 
 * Maps card types to their respective creators:
 * - CREDIT → CreditCardCreator
 * - DEBIT → DebitCardCreator
 * - PREPAID → DebitCardCreator (prepaid uses debit strategy)
 * 
 * This factory enables the Strategy pattern, allowing card creation logic
 * to vary based on the card type without conditional statements in service layer.
 */
@Component
@RequiredArgsConstructor
public class CardCreatorFactory {

    private final CreditCardCreator creditCardCreator;
    private final DebitCardCreator debitCardCreator;

    /**
     * Get the appropriate card creator for the given card type.
     * 
     * @param cardType the type of card to create (CREDIT, DEBIT, PREPAID)
     * @return the CardCreator strategy for that type
     * @throws CreateCardException if card type is unsupported
     */
    public CardCreator getCreator(CardType cardType) throws CreateCardException {
        if (cardType == null) {
            throw new CreateCardException("Card type cannot be null");
        }

        return switch (cardType) {
            case CREDIT -> creditCardCreator;
            case DEBIT -> debitCardCreator;
            case PREPAID -> debitCardCreator; // Prepaid uses debit strategy
            default -> throw new CreateCardException("Unsupported card type: " + cardType);
        };
    }
}
