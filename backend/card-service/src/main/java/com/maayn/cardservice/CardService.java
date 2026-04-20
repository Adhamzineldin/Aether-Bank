package com.maayn.cardservice;

import com.maayn.cardservice.service.*;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.*;
import maayn.veld.generated.models.card.*;
import maayn.veld.generated.services.ICardService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardService implements ICardService {

    private final CardDetailsQueryService cardDetailsQueryService;
    private final CardPaymentService cardPaymentService;
    private final CardRefundService cardRefundService;
    private final CardVoidService cardVoidService;
    private final CardTransactionHistoryService cardTransactionHistoryService;

    @Override
    public CardDetailsResponse getCardDetails(String cardId) throws GetCardDetailsException, Exception {
        return cardDetailsQueryService.getCardDetails(cardId);
    }

    @Override
    public CardTransactionResponse processMerchantPayment(MerchantPaymentRequest input) throws ProcessMerchantPaymentException, Exception {
        return cardPaymentService.process(input);
    }

    @Override
    public CardTransactionResponse refundTransaction(RefundCardTransactionRequest input) throws RefundTransactionException, Exception {
        return cardRefundService.refund(input);
    }

    @Override
    public CardTransactionResponse voidTransaction(VoidCardTransactionRequest input) throws VoidTransactionException, Exception {
        return cardVoidService.voidTransaction(input);
    }

    @Override
    public PaginatedCardTransactionResponse getCardTransactions(String cardId, GetCardTransactionsRequest input) throws GetCardTransactionsException, Exception {
        return cardTransactionHistoryService.getTransactions(cardId, input);
    }
}
