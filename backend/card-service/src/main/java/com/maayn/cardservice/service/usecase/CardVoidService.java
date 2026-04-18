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
import maayn.veld.generated.errors.VoidTransactionException;
import maayn.veld.generated.models.card.CardTransactionResponse;
import maayn.veld.generated.models.card.VoidCardTransactionRequest;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionResponse;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardVoidService {

    private final CardAccessService cardAccessService;
    private final CardRulesValidator cardRulesValidator;
    private final TransactionGateway transactionGateway;
    private final CardTransactionFactory cardTransactionFactory;
    private final CreditBalanceService creditBalanceService;
    private final CardTransactionRepository cardTransactionRepository;
    
    
    //TODO: USE Dependency Injections like professionals
    public CardVoidService(
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
    public CardTransactionResponse voidTransaction(VoidCardTransactionRequest input) throws VoidTransactionException, Exception {
        CardTransaction original = cardAccessService.getVoidableTransaction(input.getTransactionId());
        cardRulesValidator.validateVoid(original);
        String idempotencyKey = "void-" + original.getId();

        CardTransaction cached = cardAccessService.findTransactionByIdempotencyKey(idempotencyKey).orElse(null);
        if (cached != null) {
            return CardMapper.toTransactionResponse(cached);
        }

        TransactionResponse transferResult;
        try {
            transferResult = transactionGateway.transfer(
                    SystemAccounts.CASH_VAULT_ID,
                    original.getCard().getAccountId(),
                    original.getAmount(),
                    original.getCurrency(),
                    idempotencyKey,
                    TransactionType.INTERNAL_TRANSFER
            );
        } catch (TransactionGatewayException ex) {
            throw CardErrors.voidTransaction.voidNotAllowed(ex.getMessage());
        }

        CardTransaction voidTransaction = cardTransactionFactory.createVoid(
                original,
                idempotencyKey,
                transferResult.getReferenceNumber()
        );

        creditBalanceService.reverseCharge(original.getCard(), original.getAmount());
        original.setStatus(maayn.veld.generated.models.card.CardTransactionStatus.VOIDED);
        cardTransactionRepository.save(voidTransaction);
        return CardMapper.toTransactionResponse(voidTransaction);
    }
}
