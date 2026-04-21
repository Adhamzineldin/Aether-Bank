package com.maayn.cardservice.service;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.exception.TransactionGatewayException;
import com.maayn.cardservice.gateway.TransactionGateway;
import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.payment.PaymentFlowFactory;
import com.maayn.cardservice.repository.CardTransactionRepository;
import com.maayn.cardservice.validator.CardRulesValidator;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.CardErrors;
import maayn.veld.generated.errors.ProcessMerchantPaymentException;
import maayn.veld.generated.models.card.CardTransactionResponse;
import maayn.veld.generated.models.card.MerchantPaymentRequest;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionResponse;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardPaymentService {

    private final CardAccessService cardAccessService;
    private final CardRulesValidator cardRulesValidator;
    private final TransactionGateway transactionGateway;
    private final CardTransactionFactory cardTransactionFactory;
    private final PaymentFlowFactory paymentFlowFactory;
    private final CardTransactionRepository cardTransactionRepository;

    @Transactional
    public CardTransactionResponse process(MerchantPaymentRequest input) throws ProcessMerchantPaymentException {
        cardRulesValidator.validateMerchantPaymentRequest(input);
        String idempotencyKey = resolveIdempotencyKey(input);

        var cached = cardAccessService.findTransactionByIdempotencyKey(idempotencyKey);
        if (cached.isPresent()) return CardMapper.toTransactionResponse(cached.get());
        return executePayment(input, idempotencyKey);
    }

    private CardTransactionResponse executePayment(MerchantPaymentRequest input, String idempotencyKey) {
        Card card = cardAccessService.getCardByToken(input.getCardToken());
        cardRulesValidator.validateCardForPayment(card, input.getCurrency(), input.getAmount());

        TransactionResponse transfer = transferFunds(card, input, idempotencyKey);
        CardTransaction tx = cardTransactionFactory.createPurchase(
                card, input.getMerchantId(), idempotencyKey,
                transfer.getReferenceNumber(), input.getAmount(), input.getCurrency());

        paymentFlowFactory.create(card).applyPostTransferEffects(card, input.getAmount());
        cardTransactionRepository.save(tx);
        return CardMapper.toTransactionResponse(tx);
    }

    private TransactionResponse transferFunds(Card card, MerchantPaymentRequest input, String idempotencyKey) {
        try {
            return transactionGateway.transfer(
                    card.getAccountId(), SystemAccounts.CASH_VAULT_ID,
                    input.getAmount(), input.getCurrency(), idempotencyKey, TransactionType.CARD_PAYMENT);
        } catch (TransactionGatewayException ex) {
            throw mapGatewayError(ex);
        }
    }

    private ProcessMerchantPaymentException mapGatewayError(TransactionGatewayException ex) {
        return switch (ex.getReason()) {
            case INVALID_AMOUNT -> CardErrors.processMerchantPayment.invalidAmount(ex.getMessage());
            case INSUFFICIENT_FUNDS -> CardErrors.processMerchantPayment.insufficientCredit(ex.getMessage());
            case UNKNOWN -> throw ex;
        };
    }

    private String resolveIdempotencyKey(MerchantPaymentRequest input) {
        if (input.getIdempotencyKey() != null && !input.getIdempotencyKey().isBlank()) {
            return input.getIdempotencyKey();
        }
        return "card-payment-" + input.getCardToken() + "-" + cardRulesValidator.normalizeCurrency(input.getCurrency()) + "-" + input.getAmount();
    }
}
