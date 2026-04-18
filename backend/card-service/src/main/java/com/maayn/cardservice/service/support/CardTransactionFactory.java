package com.maayn.cardservice.service.support;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import maayn.veld.generated.models.card.CardTransactionStatus;
import maayn.veld.generated.models.card.CardTransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@Component
/**
 * Builds the local card transaction entities that mirror successful ledger operations.
 * All transaction variants share the same base metadata and only differ in type/status/original link.
 */
public class CardTransactionFactory {

    private static final BigDecimal ONE = BigDecimal.ONE;

    public CardTransaction createPurchase(
            Card card,
            UUID merchantId,
            String idempotencyKey,
            String ledgerReference,
            BigDecimal amount,
            String currency
    ) {
        CardTransaction transaction = baseTransaction(card, merchantId, idempotencyKey, ledgerReference, amount, currency);
        transaction.setStatus(CardTransactionStatus.APPROVED);
        transaction.setType(CardTransactionType.PURCHASE);
        return transaction;
    }

    public CardTransaction createRefund(CardTransaction original, String idempotencyKey, String ledgerReference, BigDecimal amount) {
        CardTransaction transaction = baseTransaction(
                original.getCard(),
                original.getMerchantId(),
                idempotencyKey,
                ledgerReference,
                amount,
                original.getCurrency()
        );
        transaction.setOriginalTransactionId(original.getId());
        transaction.setStatus(CardTransactionStatus.REFUNDED);
        transaction.setType(CardTransactionType.REFUND);
        return transaction;
    }

    public CardTransaction createVoid(CardTransaction original, String idempotencyKey, String ledgerReference) {
        CardTransaction transaction = baseTransaction(
                original.getCard(),
                original.getMerchantId(),
                idempotencyKey,
                ledgerReference,
                original.getAmount(),
                original.getCurrency()
        );
        transaction.setOriginalTransactionId(original.getId());
        transaction.setStatus(CardTransactionStatus.VOIDED);
        transaction.setType(CardTransactionType.VOID);
        return transaction;
    }

    private CardTransaction baseTransaction(
            Card card,
            UUID merchantId,
            String idempotencyKey,
            String ledgerReference,
            BigDecimal amount,
            String currency
    ) {
        CardTransaction transaction = new CardTransaction();
        transaction.setCard(card);
        transaction.setMerchantId(merchantId);
        // Auth codes are generated locally for card-domain tracking and customer support workflows.
        transaction.setAuthCode(generateAuthCode());
        transaction.setLedgerReference(ledgerReference);
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setAmount(amount);
        transaction.setCurrency(currency.trim().toUpperCase(Locale.ROOT));
        // No FX conversion is implemented yet, so base currency values mirror the transaction currency.
        transaction.setAmountInBaseCurrency(amount);
        transaction.setExchangeRate(ONE);
        return transaction;
    }

    private String generateAuthCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
