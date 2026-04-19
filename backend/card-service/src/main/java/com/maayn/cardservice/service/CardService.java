package com.maayn.cardservice.service;

import com.maayn.cardservice.service.usecase.CardDetailsQueryService;
import com.maayn.cardservice.service.usecase.CardPaymentService;
import com.maayn.cardservice.service.usecase.CardRefundService;
import com.maayn.cardservice.service.usecase.CardTransactionHistoryService;
import com.maayn.cardservice.service.usecase.CardVoidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.errors.GetCardDetailsException;
import maayn.veld.generated.errors.GetCardTransactionsException;
import maayn.veld.generated.errors.ProcessMerchantPaymentException;
import maayn.veld.generated.errors.RefundTransactionException;
import maayn.veld.generated.errors.VoidTransactionException;
import maayn.veld.generated.models.card.CardDetailsResponse;
import maayn.veld.generated.models.card.CardTransactionResponse;
import maayn.veld.generated.models.card.GetCardTransactionsRequest;
import maayn.veld.generated.models.card.MerchantPaymentRequest;
import maayn.veld.generated.models.card.PaginatedCardTransactionResponse;
import maayn.veld.generated.models.card.RefundCardTransactionRequest;
import maayn.veld.generated.models.card.VoidCardTransactionRequest;
import maayn.veld.generated.services.ICardService;
import org.springframework.stereotype.Service;

/**
 * Main entry point for the card service, exposed to the generated controller layer.
 * Implements ICardService from VELD for proper contract definition.
 * Delegates each operation to focused use-case services while maintaining professional DI.
 * 
 * Follows transaction service patterns:
 * - Uses @RequiredArgsConstructor for zero-boilerplate DI
 * - Adds @Slf4j for structured logging
 * - Delegates to specialized services for each operation
 * - Clear separation of concerns with single responsibility
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardService implements ICardService {

    private final CardDetailsQueryService cardDetailsQueryService;
    private final CardPaymentService cardPaymentService;
    private final CardRefundService cardRefundService;
    private final CardVoidService cardVoidService;
    private final CardTransactionHistoryService cardTransactionHistoryService;

    /**
     * Retrieves card details by card ID.
     * Read-only operation delegated to CardDetailsQueryService.
     * 
     * @param cardId the card identifier
     * @return card details response
     * @throws GetCardDetailsException if card not found or operation fails
     */
    @Override
    public CardDetailsResponse getCardDetails(String cardId) throws GetCardDetailsException, Exception {
        log.info("Fetching card details for card: {}", cardId);
        return cardDetailsQueryService.getCardDetails(cardId);
    }

    /**
     * Processes a merchant payment transaction on the card.
     * Validates request, moves funds, and persists transaction state.
     * 
     * @param input the merchant payment request
     * @return card transaction response with auth code
     * @throws ProcessMerchantPaymentException if payment processing fails
     */
    @Override
    public CardTransactionResponse processMerchantPayment(MerchantPaymentRequest input) 
            throws ProcessMerchantPaymentException, Exception {
        log.info("Processing merchant payment of {} {}", input.getAmount(), input.getCurrency());
        return cardPaymentService.process(input);
    }

    /**
     * Refunds a previously processed card transaction.
     * Validates the original purchase and reverses the transfer.
     * 
     * @param input the refund request containing original transaction reference
     * @return card transaction response for the refund
     * @throws RefundTransactionException if refund processing fails
     */
    @Override
    public CardTransactionResponse refundTransaction(RefundCardTransactionRequest input) 
            throws RefundTransactionException, Exception {
        log.info("Processing refund for transaction: {}", input.getTransactionId());
        return cardRefundService.refund(input);
    }

    /**
     * Voids a recently approved card transaction without moving funds.
     * Restores the card balance to pre-authorization state.
     * 
     * @param input the void request containing transaction to cancel
     * @return card transaction response for the void
     * @throws VoidTransactionException if void processing fails
     */
    @Override
    public CardTransactionResponse voidTransaction(VoidCardTransactionRequest input) 
            throws VoidTransactionException, Exception {
        log.info("Processing void for transaction: {}", input.getTransactionId());
        return cardVoidService.voidTransaction(input);
    }

    /**
     * Retrieves paginated transaction history for a card.
     * Supports filtering by status and transaction type.
     * 
     * @param cardId the card identifier
     * @param input pagination and filter criteria
     * @return paginated card transaction response
     * @throws GetCardTransactionsException if query fails
     */
    @Override
    public PaginatedCardTransactionResponse getCardTransactions(String cardId, GetCardTransactionsRequest input) 
            throws GetCardTransactionsException, Exception {
        log.info("Fetching transaction history for card: {} with page: {}", 
            cardId, input.getPageNumber());
        return cardTransactionHistoryService.getTransactions(cardId, input);
    }
}
