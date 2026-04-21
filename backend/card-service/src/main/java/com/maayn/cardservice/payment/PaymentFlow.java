package com.maayn.cardservice.payment;

import com.maayn.cardservice.entity.Card;

import java.math.BigDecimal;

public interface PaymentFlow {

    void applyPostTransferEffects(Card card, BigDecimal amount);

    PaymentFlowType getFlowType();
}
