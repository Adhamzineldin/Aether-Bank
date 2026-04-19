package com.maayn.cardservice.mapper;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.entity.CreditCardDetailsEntity;
import maayn.veld.generated.models.card.CardDetailsResponse;
import maayn.veld.generated.models.card.CardSummary;
import maayn.veld.generated.models.card.CardTransactionResponse;
import maayn.veld.generated.models.card.CreditCardDetails;
import maayn.veld.generated.models.card.PaginatedCardTransactionResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public final class CardMapper {

    private CardMapper() {
    }

    public static CardDetailsResponse toCardDetailsResponse(Card card) {
        return new CardDetailsResponse(toCardSummary(card), toCreditCardDetails(card.getCreditDetails()));
    }

    public static CardSummary toCardSummary(Card card) {
        return new CardSummary(
                card.getId(),
                card.getAccountId(),
                card.getCustomerId(),
                card.getCardToken(),
                card.getLastFourDigits(),
                card.getCardType(),
                card.getCardNetwork(),
                card.getStatus(),
                card.getExpiryMonth(),
                card.getExpiryYear(),
                card.getIssuedAt(),
                card.getActivatedAt(),
                card.getBlockedAt(),
                card.getBlockReason()
        );
    }

    public static CreditCardDetails toCreditCardDetails(CreditCardDetailsEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CreditCardDetails(
                entity.getCreditLimit(),
                entity.getAvailableCredit(),
                entity.getCurrentBalance(),
                entity.getMinimumPayment(),
                entity.getPaymentDueDate(),
                entity.getAnnualInterestRate(),
                entity.getBillingCycleDay(),
                entity.getLastStatementDate()
        );
    }

    public static CardTransactionResponse toTransactionResponse(CardTransaction entity) {
        return new CardTransactionResponse(
                entity.getId(),
                entity.getCard().getId(),
                entity.getMerchantId(),
                entity.getAuthCode(),
                null,
                entity.getAmount(),
                entity.getCurrency(),
                entity.getAmountInBaseCurrency(),
                entity.getExchangeRate(),
                entity.getStatus(),
                entity.getType(),
                entity.getProcessedAt()
        );
    }

    public static PaginatedCardTransactionResponse toPaginatedResponse(Page<CardTransaction> page) {
        List<CardTransactionResponse> content = page.getContent().stream()
                .map(CardMapper::toTransactionResponse)
                .toList();

        return new PaginatedCardTransactionResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
