package com.maayn.cardservice.payment;

import com.maayn.cardservice.entity.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFlowFactory {

    private final CreditCardPaymentFlow creditCardPaymentFlow;
    private final DebitCardPaymentFlow debitCardPaymentFlow;

    public PaymentFlow create(Card card) {
        return switch (card.getCardType()) {
            case CREDIT -> creditCardPaymentFlow;
            case DEBIT -> debitCardPaymentFlow;
        };
    }
}
