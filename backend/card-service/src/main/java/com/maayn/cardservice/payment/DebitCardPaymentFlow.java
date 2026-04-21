package com.maayn.cardservice.payment;

import com.maayn.cardservice.entity.Card;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DebitCardPaymentFlow implements PaymentFlow {

    @Override
    public void applyPostTransferEffects(Card card, BigDecimal amount) {
        // Debit funds are settled immediately via the ledger; no local credit tracking needed.
    }

    @Override
    public PaymentFlowType getFlowType() {
        return PaymentFlowType.DEBIT_CARD;
    }
}
