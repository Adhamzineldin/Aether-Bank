package com.maayn.cardservice.service;

import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.exception.TransactionGatewayException;
import com.maayn.cardservice.gateway.TransactionGateway;
import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.repository.CardTransactionRepository;
import com.maayn.cardservice.validator.CardRulesValidator;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.CardErrors;
import maayn.veld.generated.errors.RefundTransactionException;
import maayn.veld.generated.models.card.CardTransactionResponse;
import maayn.veld.generated.models.card.CardTransactionStatus;
import maayn.veld.generated.models.card.RefundCardTransactionRequest;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionResponse;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CardRefundService {

    private final CardAccessService cardAccessService;
    private final CardRulesValidator cardRulesValidator;
    private final TransactionGateway transactionGateway;
    private final CardTransactionFactory cardTransactionFactory;
    private final CreditBalanceService creditBalanceService;
    private final CardTransactionRepository cardTransactionRepository;

    @Transactional
    public CardTransactionResponse refund(RefundCardTransactionRequest input) throws RefundTransactionException {
        CardTransaction original = cardAccessService.getRefundableTransaction(input.getTransactionId());
        BigDecimal refundAmount = cardRulesValidator.validateRefund(original, input.getAmount());
        String idempotencyKey = "refund-" + original.getId();

        var cached = cardAccessService.findTransactionByIdempotencyKey(idempotencyKey);
        if (cached.isPresent()) return CardMapper.toTransactionResponse(cached.get());
        return executeRefund(original, refundAmount, idempotencyKey);
    }

    private CardTransactionResponse executeRefund(CardTransaction original, BigDecimal amount, String idempotencyKey) {
        TransactionResponse transfer = transferRefund(original, amount, idempotencyKey);
        CardTransaction refundTx = cardTransactionFactory.createRefund(original, idempotencyKey, transfer.getReferenceNumber(), amount);

        creditBalanceService.reverseCharge(original.getCard(), amount);
        original.setStatus(CardTransactionStatus.REFUNDED);
        cardTransactionRepository.save(refundTx);
        return CardMapper.toTransactionResponse(refundTx);
    }

    private TransactionResponse transferRefund(CardTransaction original, BigDecimal amount, String idempotencyKey) {
        try {
            return transactionGateway.transfer(
                    SystemAccounts.CASH_VAULT_ID, original.getCard().getAccountId(),
                    amount, original.getCurrency(), idempotencyKey, TransactionType.INTERNAL_TRANSFER);
        } catch (TransactionGatewayException ex) {
            throw mapGatewayError(ex);
        }
    }

    private RefundTransactionException mapGatewayError(TransactionGatewayException ex) {
        return switch (ex.getReason()) {
            case INVALID_AMOUNT -> CardErrors.refundTransaction.invalidAmount(ex.getMessage());
            case INSUFFICIENT_FUNDS, UNKNOWN -> CardErrors.refundTransaction.refundNotAllowed(ex.getMessage());
        };
    }
}
