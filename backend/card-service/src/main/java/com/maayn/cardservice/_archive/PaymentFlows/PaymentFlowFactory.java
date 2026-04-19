package com.maayn.cardservice.PaymentFlows;

import com.maayn.cardservice.entity.Card;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.card.CardType;
import org.springframework.stereotype.Component;

/**
 * Payment Flow Factory (SOLID: Open/Closed - extensible for new payment types).
 * Creates appropriate payment flow based on card type.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFlowFactory {

    private final CreditCardPaymentFlow creditCardPaymentFlow;
    private final DebitCardPaymentFlow debitCardPaymentFlow;

    /**
     * Factory method to create payment flow based on card type.
     * 
     * @param card the card being used for payment
     * @return appropriate PaymentFlow implementation
     */
    public PaymentFlow createPaymentFlow(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }

        CardType cardType = card.getCardType();
        
        log.debug("Creating payment flow for card type: {}", cardType);

        return switch (cardType) {
            case CREDIT -> {
                log.debug("Creating CREDIT card payment flow");
                yield creditCardPaymentFlow;
            }
            case DEBIT -> {
                log.debug("Creating DEBIT card payment flow");
                yield debitCardPaymentFlow;
            }
            default -> throw new IllegalArgumentException("Unsupported card type: " + cardType);
        };
    }
}
