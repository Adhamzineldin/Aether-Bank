package com.maayn.cardservice.service;

import com.maayn.cardservice.service.usecase.CardDetailsQueryService;
import com.maayn.cardservice.service.usecase.CardPaymentService;
import com.maayn.cardservice.service.usecase.CardRefundService;
import com.maayn.cardservice.service.usecase.CardTransactionHistoryService;
import com.maayn.cardservice.service.usecase.CardVoidService;
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

@Service
/**
 * Entry point exposed to the generated controller layer.
 * This class keeps the API surface small and delegates each operation to a focused use-case service.
 */
public class CardService implements ICardService {

    private final CardDetailsQueryService cardDetailsQueryService;
    private final CardPaymentService cardPaymentService;
    private final CardRefundService cardRefundService;
    private final CardVoidService cardVoidService;
    private final CardTransactionHistoryService cardTransactionHistoryService;

    public CardService(
            CardDetailsQueryService cardDetailsQueryService,
            CardPaymentService cardPaymentService,
            CardRefundService cardRefundService,
            CardVoidService cardVoidService,
            CardTransactionHistoryService cardTransactionHistoryService
    ) {
        this.cardDetailsQueryService = cardDetailsQueryService;
        this.cardPaymentService = cardPaymentService;
        this.cardRefundService = cardRefundService;
        this.cardVoidService = cardVoidService;
        this.cardTransactionHistoryService = cardTransactionHistoryService;
    }

    @Override
    public CardDetailsResponse getCardDetails(String cardId) throws GetCardDetailsException, Exception {
        // Read-only card lookup handled by the dedicated query service.
        return cardDetailsQueryService.getCardDetails(cardId);
    }

    @Override
    public CardTransactionResponse processMerchantPayment(MerchantPaymentRequest input) throws ProcessMerchantPaymentException, Exception {
        // Purchase flow: validate request, move funds, persist local transaction state.
        return cardPaymentService.process(input);
    }

    @Override
    public CardTransactionResponse refundTransaction(RefundCardTransactionRequest input) throws RefundTransactionException, Exception {
        // Refund flow: validate the original purchase, reverse the transfer, record the refund.
        return cardRefundService.refund(input);
    }

    @Override
    public CardTransactionResponse voidTransaction(VoidCardTransactionRequest input) throws VoidTransactionException, Exception {
        // Void flow: cancel a recent approved purchase and restore the card balance.
        return cardVoidService.voidTransaction(input);
    }

    @Override
    public PaginatedCardTransactionResponse getCardTransactions(String cardId, GetCardTransactionsRequest input) throws GetCardTransactionsException, Exception {
        // Transaction history supports pagination plus optional status/type filters.
        return cardTransactionHistoryService.getTransactions(cardId, input);
    }
}
