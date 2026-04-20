package com.maayn.cardservice.service;

import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.exception.TransactionGatewayException;
import com.maayn.cardservice.gateway.TransactionGateway;
import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.repository.CardTransactionRepository;
import com.maayn.cardservice.validator.CardRulesValidator;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.CardErrors;
import maayn.veld.generated.errors.VoidTransactionException;
import maayn.veld.generated.models.card.CardTransactionResponse;
import maayn.veld.generated.models.card.CardTransactionStatus;
import maayn.veld.generated.models.card.VoidCardTransactionRequest;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionResponse;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardVoidService {

    private final CardAccessService cardAccessService;
    private final CardRulesValidator cardRulesValidator;
    private final TransactionGateway transactionGateway;
    private final CardTransactionFactory cardTransactionFactory;
    private final CreditBalanceService creditBalanceService;
    private final CardTransactionRepository cardTransactionRepository;

    @Transactional
    public CardTransactionResponse voidTransaction(VoidCardTransactionRequest input) throws VoidTransactionException {
        CardTransaction original = cardAccessService.getVoidableTransaction(input.getTransactionId());
        cardRulesValidator.validateVoid(original);
        String idempotencyKey = "void-" + original.getId();

        var cached = cardAccessService.findTransactionByIdempotencyKey(idempotencyKey);
        if (cached.isPresent()) return CardMapper.toTransactionResponse(cached.get());
        return executeVoid(original, idempotencyKey);
    }

    private CardTransactionResponse executeVoid(CardTransaction original, String idempotencyKey) {
        TransactionResponse transfer = transferVoid(original, idempotencyKey);
        CardTransaction voidTx = cardTransactionFactory.createVoid(original, idempotencyKey, transfer.getReferenceNumber());

        creditBalanceService.reverseCharge(original.getCard(), original.getAmount());
        original.setStatus(CardTransactionStatus.VOIDED);
        cardTransactionRepository.save(voidTx);
        return CardMapper.toTransactionResponse(voidTx);
    }

    private TransactionResponse transferVoid(CardTransaction original, String idempotencyKey) {
        try {
            return transactionGateway.transfer(
                    SystemAccounts.CASH_VAULT_ID, original.getCard().getAccountId(),
                    original.getAmount(), original.getCurrency(), idempotencyKey, TransactionType.INTERNAL_TRANSFER);
        } catch (TransactionGatewayException ex) {
            throw CardErrors.voidTransaction.voidNotAllowed(ex.getMessage());
        }
    }
}
