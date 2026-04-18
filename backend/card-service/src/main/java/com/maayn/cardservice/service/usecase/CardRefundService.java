package com.maayn.cardservice.service.usecase;

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
import maayn.veld.generated.errors.RefundTransactionException;
import maayn.veld.generated.models.card.CardTransactionResponse;
import maayn.veld.generated.models.card.RefundCardTransactionRequest;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionResponse;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
/**
 * Reverses a settled purchase by transferring money back from the bank vault to the card account.
 * The service currently supports full refunds only.
 */
public class CardRefundService {

    private final CardAccessService cardAccessService;
    private final CardRulesValidator cardRulesValidator;
    private final TransactionGateway transactionGateway;
    private final CardTransactionFactory cardTransactionFactory;
    private final CreditBalanceService creditBalanceService;
    private final CardTransactionRepository cardTransactionRepository;
    //TODO: USE Dependency Injections like professionals
    public CardRefundService(
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
    public CardTransactionResponse refund(RefundCardTransactionRequest input) throws RefundTransactionException, Exception {
        CardTransaction original = cardAccessService.getRefundableTransaction(input.getTransactionId());
        BigDecimal refundAmount = cardRulesValidator.validateRefund(original, input.getAmount());
        String idempotencyKey = "refund-" + original.getId();

        // Refunds are idempotent per original transaction so repeated requests return the same result.
        CardTransaction cached = cardAccessService.findTransactionByIdempotencyKey(idempotencyKey).orElse(null);
        if (cached != null) {
            return CardMapper.toTransactionResponse(cached);
        }

        TransactionResponse transferResult;
        try {
            transferResult = transactionGateway.transfer(
                    SystemAccounts.CASH_VAULT_ID,
                    original.getCard().getAccountId(),
                    refundAmount,
                    original.getCurrency(),
                    idempotencyKey,
                    TransactionType.INTERNAL_TRANSFER
            );
        } catch (TransactionGatewayException ex) {
            throw mapRefundGatewayFailure(ex);
        }

        CardTransaction refundTransaction = cardTransactionFactory.createRefund(
                original,
                idempotencyKey,
                transferResult.getReferenceNumber(),
                refundAmount
        );

        // Restore the customer credit headroom and mark the original purchase as refunded.
        creditBalanceService.reverseCharge(original.getCard(), refundAmount);
        original.setStatus(maayn.veld.generated.models.card.CardTransactionStatus.REFUNDED);
        cardTransactionRepository.save(refundTransaction);
        return CardMapper.toTransactionResponse(refundTransaction);
    }

    private RefundTransactionException mapRefundGatewayFailure(TransactionGatewayException ex) throws RefundTransactionException {
        return switch (ex.getReason()) {
            case INVALID_AMOUNT -> CardErrors.refundTransaction.invalidAmount(ex.getMessage());
            case INSUFFICIENT_FUNDS, UNKNOWN -> CardErrors.refundTransaction.refundNotAllowed(ex.getMessage());
        };
    }
}
