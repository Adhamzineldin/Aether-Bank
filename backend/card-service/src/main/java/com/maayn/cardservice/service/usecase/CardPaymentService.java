package com.maayn.cardservice.service.usecase;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.exception.TransactionGatewayException;
import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.repository.CardTransactionRepository;
import com.maayn.cardservice.service.support.CardAccessService;
import com.maayn.cardservice.service.support.CardRulesValidator;
import com.maayn.cardservice.service.support.CardTransactionFactory;
import com.maayn.cardservice.service.support.CreditBalanceService;
import com.maayn.cardservice.service.support.TransactionGateway;
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
public class CardPaymentService {

    private final CardAccessService cardAccessService;
    private final CardRulesValidator cardRulesValidator;
    private final TransactionGateway transactionGateway;
    private final CardTransactionFactory cardTransactionFactory;
    private final CreditBalanceService creditBalanceService;
    private final CardTransactionRepository cardTransactionRepository;

    public CardPaymentService(
            CardAccessService cardAccessService,
            CardRulesValidator cardRulesValidator,
            TransactionGateway transactionGateway,
            CardTransactionFactory cardTransactionFactory,
            CreditBalanceService creditBalanceService,
            CardTransactionRepository cardTransactionRepository
    ) {
        this.cardAccessService = cardAccessService;
        this.cardRulesValidator = cardRulesValidator;
        this.transactionGateway = transactionGateway;
        this.cardTransactionFactory = cardTransactionFactory;
        this.creditBalanceService = creditBalanceService;
        this.cardTransactionRepository = cardTransactionRepository;
    }

    @Transactional
    public CardTransactionResponse process(MerchantPaymentRequest input) throws ProcessMerchantPaymentException, Exception {
        cardRulesValidator.validateMerchantPaymentRequest(input);

        String idempotencyKey = resolvePaymentIdempotencyKey(input);
        CardTransaction cached = cardAccessService.findTransactionByIdempotencyKey(idempotencyKey).orElse(null);
        if (cached != null) {
            return CardMapper.toTransactionResponse(cached);
        }

        Card card = cardAccessService.getCardByToken(input.getCardToken());
        cardRulesValidator.validateCardForPayment(card, input.getCurrency(), input.getAmount());

        TransactionResponse transferResult;
        try {
            transferResult = transactionGateway.transfer(
                    card.getAccountId(),
                    SystemAccounts.CASH_VAULT_ID,
                    input.getAmount(),
                    input.getCurrency(),
                    idempotencyKey,
                    TransactionType.CARD_PAYMENT
            );
        } catch (TransactionGatewayException ex) {
            throw mapPaymentGatewayFailure(ex);
        }

        CardTransaction transaction = cardTransactionFactory.createPurchase(
                card,
                input.getMerchantId(),
                idempotencyKey,
                transferResult.getReferenceNumber(),
                input.getAmount(),
                input.getCurrency()
        );

        creditBalanceService.applyCharge(card, input.getAmount());
        cardTransactionRepository.save(transaction);
        return CardMapper.toTransactionResponse(transaction);
    }

    private ProcessMerchantPaymentException mapPaymentGatewayFailure(TransactionGatewayException ex) throws ProcessMerchantPaymentException {
        return switch (ex.getReason()) {
            case INVALID_AMOUNT -> CardErrors.processMerchantPayment.invalidAmount(ex.getMessage());
            case INSUFFICIENT_FUNDS -> CardErrors.processMerchantPayment.insufficientCredit(ex.getMessage());
            case UNKNOWN -> throw ex;
        };
    }

    private String resolvePaymentIdempotencyKey(MerchantPaymentRequest input) {
        if (input.getIdempotencyKey() != null && !input.getIdempotencyKey().trim().isEmpty()) {
            return input.getIdempotencyKey();
        }
        return "card-payment-" + input.getCardToken() + "-" + cardRulesValidator.normalizeCurrency(input.getCurrency()) + "-" + input.getAmount();
    }
}
