package com.maayn.cardservice.service;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.repository.CardRepository;
import com.maayn.cardservice.repository.CardTransactionRepository;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.*;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardAccessService {

    private final CardRepository cardRepository;
    private final CardTransactionRepository cardTransactionRepository;

    public Card getCardDetailsCard(String cardId) throws GetCardDetailsException {
        return cardRepository.findById(parseUuid(cardId, CardErrors.getCardDetails.notFound("Card not found")))
                .orElseThrow(() -> CardErrors.getCardDetails.notFound("Card not found"));
    }

    public UUID getExistingCardId(String cardId) throws GetCardTransactionsException {
        UUID parsedId = parseUuid(cardId, CardErrors.getCardTransactions.notFound("Card not found"));
        if (!cardRepository.existsById(parsedId)) {
            throw CardErrors.getCardTransactions.notFound("Card not found");
        }
        return parsedId;
    }

    public Card getCardByToken(String cardToken) throws ProcessMerchantPaymentException {
        return cardRepository.findByCardToken(cardToken)
                .orElseThrow(() -> CardErrors.processMerchantPayment.invalidCardToken("Card token is invalid"));
    }

    public CardTransaction getRefundableTransaction(UUID transactionId) throws RefundTransactionException {
        return cardTransactionRepository.findById(transactionId)
                .orElseThrow(() -> CardErrors.refundTransaction.transactionNotFound("Card transaction not found"));
    }

    public CardTransaction getVoidableTransaction(UUID transactionId) throws VoidTransactionException {
        return cardTransactionRepository.findById(transactionId)
                .orElseThrow(() -> CardErrors.voidTransaction.transactionNotFound("Card transaction not found"));
    }

    public Optional<CardTransaction> findTransactionByIdempotencyKey(String idempotencyKey) {
        return cardTransactionRepository.findByIdempotencyKey(idempotencyKey);
    }

    private <E extends RuntimeException> UUID parseUuid(String raw, E fallbackException) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw fallbackException;
        }
    }
}
