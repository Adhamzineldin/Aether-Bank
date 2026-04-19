package com.maayn.cardservice.service.support;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CreditCardDetailsEntity;
import com.maayn.cardservice.repository.CreditCardDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.card.CardType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Maintains credit-card-only balance fields stored in the card service database.
 * Debit-style cards bypass this service because their funds live in transaction/account services.
 * 
 * Responsibilities:
 * - Apply charges to credit card (reduce available credit, increase balance)
 * - Reverse charges (refunds/voids - restore credit, reduce balance)
 * - Calculate minimum payment due
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditBalanceService {

    private static final BigDecimal MINIMUM_PAYMENT_RATE = new BigDecimal("0.05");

    private final CreditCardDetailsRepository creditCardDetailsRepository;

    /**
     * Applies a charge to a credit card.
     * Updates available credit, current balance, and minimum payment due.
     * No-op for debit/prepaid cards.
     * 
     * @param card the card entity
     * @param amount the charge amount
     */
    public void applyCharge(Card card, BigDecimal amount) {
        log.debug("Applying charge of {} to card {}", amount, card.getId());
        
        if (card.getCardType() != CardType.CREDIT || card.getCreditDetails() == null) {
            return;
        }
        
        CreditCardDetailsEntity creditDetails = card.getCreditDetails();
        creditDetails.setAvailableCredit(creditDetails.getAvailableCredit().subtract(amount));
        creditDetails.setCurrentBalance(creditDetails.getCurrentBalance().add(amount));
        creditDetails.setMinimumPayment(creditDetails.getCurrentBalance().multiply(MINIMUM_PAYMENT_RATE).max(BigDecimal.ZERO));
        
        creditCardDetailsRepository.save(creditDetails);
        log.info("Charge applied. New available credit: {}, balance: {}", 
            creditDetails.getAvailableCredit(), creditDetails.getCurrentBalance());
    }

    /**
     * Reverses a charge (refund or void).
     * Restores available credit and reduces balance without going negative.
     * No-op for debit/prepaid cards.
     * 
     * @param card the card entity
     * @param amount the amount to reverse
     */
    public void reverseCharge(Card card, BigDecimal amount) {
        log.debug("Reversing charge of {} from card {}", amount, card.getId());
        
        if (card.getCardType() != CardType.CREDIT || card.getCreditDetails() == null) {
            return;
        }
        
        CreditCardDetailsEntity creditDetails = card.getCreditDetails();
        creditDetails.setAvailableCredit(creditDetails.getAvailableCredit().add(amount));
        creditDetails.setCurrentBalance(creditDetails.getCurrentBalance().subtract(amount).max(BigDecimal.ZERO));
        creditDetails.setMinimumPayment(creditDetails.getCurrentBalance().multiply(MINIMUM_PAYMENT_RATE).max(BigDecimal.ZERO));
        
        creditCardDetailsRepository.save(creditDetails);
        log.info("Charge reversed. New available credit: {}, balance: {}", 
            creditDetails.getAvailableCredit(), creditDetails.getCurrentBalance());
    }
}
