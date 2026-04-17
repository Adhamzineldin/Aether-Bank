package com.maayn.cardservice.service.support;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CreditCardDetailsEntity;
import com.maayn.cardservice.repository.CreditCardDetailsRepository;
import maayn.veld.generated.models.card.CardType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CreditBalanceService {

    private static final BigDecimal MINIMUM_PAYMENT_RATE = new BigDecimal("0.05");

    private final CreditCardDetailsRepository creditCardDetailsRepository;

    public CreditBalanceService(CreditCardDetailsRepository creditCardDetailsRepository) {
        this.creditCardDetailsRepository = creditCardDetailsRepository;
    }

    public void applyCharge(Card card, BigDecimal amount) {
        if (card.getCardType() != CardType.CREDIT || card.getCreditDetails() == null) {
            return;
        }
        CreditCardDetailsEntity creditDetails = card.getCreditDetails();
        creditDetails.setAvailableCredit(creditDetails.getAvailableCredit().subtract(amount));
        creditDetails.setCurrentBalance(creditDetails.getCurrentBalance().add(amount));
        creditDetails.setMinimumPayment(creditDetails.getCurrentBalance().multiply(MINIMUM_PAYMENT_RATE).max(BigDecimal.ZERO));
        creditCardDetailsRepository.save(creditDetails);
    }

    public void reverseCharge(Card card, BigDecimal amount) {
        if (card.getCardType() != CardType.CREDIT || card.getCreditDetails() == null) {
            return;
        }
        CreditCardDetailsEntity creditDetails = card.getCreditDetails();
        creditDetails.setAvailableCredit(creditDetails.getAvailableCredit().add(amount));
        creditDetails.setCurrentBalance(creditDetails.getCurrentBalance().subtract(amount).max(BigDecimal.ZERO));
        creditDetails.setMinimumPayment(creditDetails.getCurrentBalance().multiply(MINIMUM_PAYMENT_RATE).max(BigDecimal.ZERO));
        creditCardDetailsRepository.save(creditDetails);
    }
}
