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

/**
 * Mapper for converting between Card service entities and VELD generated models.
 * Provides clean separation between internal entity models and external API contracts.
 * 
 * Aligned with TransactionService's TransactionMapper pattern:
 * - Static factory methods for entity → VELD model conversions
 * - Single responsibility: coordinate transformations
 * - Uses generated VELD models for API responses
 */
public final class CardMapper {

    private CardMapper() {
        // Static utility class - no instantiation
    }

    /**
     * Converts a Card entity to CardDetailsResponse for API consumption.
     * Combines card summary and credit card details if present.
     * 
     * @param card the card entity
     * @return card details response with full card information
     */
    public static CardDetailsResponse toCardDetailsResponse(Card card) {
        return new CardDetailsResponse(toCardSummary(card), toCreditCardDetails(card.getCreditDetails()));
    }

    /**
     * Converts a Card entity to CardSummary.
     * CardSummary contains basic card identification and status information.
     * 
     * @param card the card entity
     * @return card summary with public-safe information
     */
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

    /**
     * Converts CreditCardDetailsEntity to VELD's CreditCardDetails model.
     * Handles null case for non-credit cards.
     * 
     * @param entity the credit card details entity
     * @return VELD credit card details, or null if not a credit card
     */
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

    /**
     * Converts CardTransaction entity to CardTransactionResponse for API responses.
     * Maps all transaction details including amounts and statuses.
     * 
     * @param entity the card transaction entity
     * @return card transaction response
     */
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

    /**
     * Converts a paginated page of CardTransaction entities to PaginatedCardTransactionResponse.
     * Maintains pagination metadata for client-side navigation.
     * 
     * @param page the spring data page of card transactions
     * @return paginated response with transactions and metadata
     */
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
