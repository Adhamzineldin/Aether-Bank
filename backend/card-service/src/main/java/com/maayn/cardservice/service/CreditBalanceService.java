package com.maayn.cardservice.service;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CreditCardDetailsEntity;
import com.maayn.cardservice.repository.CreditCardDetailsRepository;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.models.card.CardType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CreditBalanceService {

    private static final BigDecimal MINIMUM_PAYMENT_RATE = new BigDecimal("0.05");

    private final CreditCardDetailsRepository creditCardDetailsRepository;

    public void applyCharge(Card card, BigDecimal amount) {
        if (!isCreditCard(card)) return;
        CreditCardDetailsEntity details = card.getCreditDetails();
        details.setAvailableCredit(details.getAvailableCredit().subtract(amount));
        details.setCurrentBalance(details.getCurrentBalance().add(amount));
        details.setMinimumPayment(details.getCurrentBalance().multiply(MINIMUM_PAYMENT_RATE).max(BigDecimal.ZERO));
        creditCardDetailsRepository.save(details);
    }

    public void reverseCharge(Card card, BigDecimal amount) {
        if (!isCreditCard(card)) return;
        CreditCardDetailsEntity details = card.getCreditDetails();
        details.setAvailableCredit(details.getAvailableCredit().add(amount));
        details.setCurrentBalance(details.getCurrentBalance().subtract(amount).max(BigDecimal.ZERO));
        details.setMinimumPayment(details.getCurrentBalance().multiply(MINIMUM_PAYMENT_RATE).max(BigDecimal.ZERO));
        creditCardDetailsRepository.save(details);
    }

    private boolean isCreditCard(Card card) {
        return card.getCardType() == CardType.CREDIT && card.getCreditDetails() != null;
    }
}
