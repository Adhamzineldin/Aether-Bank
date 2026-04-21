package com.maayn.cardservice.service;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import maayn.veld.generated.models.card.CardTransactionStatus;
import maayn.veld.generated.models.card.CardTransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@Component
public class CardTransactionFactory {

    public CardTransaction createPurchase(Card card, UUID merchantId, String idempotencyKey, String ledgerReference, BigDecimal amount, String currency) {
        CardTransaction tx = buildBase(card, merchantId, idempotencyKey, ledgerReference, amount, currency);
        tx.setStatus(CardTransactionStatus.APPROVED);
        tx.setType(CardTransactionType.PURCHASE);
        return tx;
    }

    public CardTransaction createRefund(CardTransaction original, String idempotencyKey, String ledgerReference, BigDecimal amount) {
        CardTransaction tx = buildBase(original.getCard(), original.getMerchantId(), idempotencyKey, ledgerReference, amount, original.getCurrency());
        tx.setOriginalTransactionId(original.getId());
        tx.setStatus(CardTransactionStatus.REFUNDED);
        tx.setType(CardTransactionType.REFUND);
        return tx;
    }

    public CardTransaction createVoid(CardTransaction original, String idempotencyKey, String ledgerReference) {
        CardTransaction tx = buildBase(original.getCard(), original.getMerchantId(), idempotencyKey, ledgerReference, original.getAmount(), original.getCurrency());
        tx.setOriginalTransactionId(original.getId());
        tx.setStatus(CardTransactionStatus.VOIDED);
        tx.setType(CardTransactionType.VOID);
        return tx;
    }

    private CardTransaction buildBase(Card card, UUID merchantId, String idempotencyKey, String ledgerReference, BigDecimal amount, String currency) {
        CardTransaction tx = new CardTransaction();
        tx.setCard(card);
        tx.setMerchantId(merchantId);
        tx.setAuthCode(generateAuthCode());
        tx.setLedgerReference(ledgerReference);
        tx.setIdempotencyKey(idempotencyKey);
        tx.setAmount(amount);
        tx.setCurrency(currency.trim().toUpperCase(Locale.ROOT));
        tx.setAmountInBaseCurrency(amount);
        tx.setExchangeRate(BigDecimal.ONE);
        return tx;
    }

    private String generateAuthCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
