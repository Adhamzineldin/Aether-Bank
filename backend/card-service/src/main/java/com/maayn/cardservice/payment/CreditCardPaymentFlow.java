package com.maayn.cardservice.payment;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.service.CreditBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CreditCardPaymentFlow implements PaymentFlow {

    private final CreditBalanceService creditBalanceService;

    @Override
    public void applyPostTransferEffects(Card card, BigDecimal amount) {
        creditBalanceService.applyCharge(card, amount);
    }

    @Override
    public PaymentFlowType getFlowType() {
        return PaymentFlowType.CREDIT_CARD;
    }
}
