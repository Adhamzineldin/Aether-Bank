package com.maayn.transactionservice.service;

import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.events.TransactionEventPublisher;
import com.maayn.transactionservice.exceptions.LedgerNotInitializedException;
import com.maayn.transactionservice.handlers.TransferIdempotencyHandler;
import com.maayn.transactionservice.mappers.TransactionMapper;
import com.maayn.transactionservice.repository.TransactionRepository;
import com.maayn.transactionservice.validators.TransactionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.errors.TransactionErrors;
import maayn.veld.generated.models.transaction.*;
import maayn.veld.generated.services.ITransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;
    private final FxRateService fxRateService;
    private final TransactionEventPublisher eventPublisher;
    private final TransactionValidator validator;
    private final TransferIdempotencyHandler idempotencyHandler;

    @Transactional
    @Override
    public TransactionResponse transfer(TransferRequest request) throws Exception {
        Optional<TransactionResponse> cached = idempotencyHandler.getIfAlreadyProcessed(request.getIdempotencyKey());
        if (cached.isPresent()) return cached.get();

        Transaction transaction = prepareTransaction(request);

        validator.validateTransfer(transaction);
        executeMathSafely(transaction);

        return persistAndDispatch(transaction);
    }

    @Transactional(readOnly = true)
    @Override
    public PaginatedTransactionResponse getAccountTransactions(String accountId, getAccountTransactionsRequest input) {
        log.info("Fetching transaction history for account {}", accountId);

        UUID accountIdAsUUID = UUID.fromString(accountId);
        Page<Transaction> page = transactionRepository.findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(
                accountIdAsUUID, accountIdAsUUID, PageRequest.of(input.getPage(), input.getPageSize())
        );

        return TransactionMapper.toPaginatedResponse(page);
    }


    private Transaction prepareTransaction(TransferRequest request) {
        Transaction transaction = TransactionMapper.toEntity(request);

        BigDecimal rate = fxRateService.getRate(transaction.getSourceCurrency(), transaction.getDestinationCurrency());
        BigDecimal destAmount = fxRateService.calculateDestinationAmount(transaction.getAmount(), rate);

        transaction.setExchangeRate(rate);
        transaction.setDestinationAmount(destAmount);

        return transaction;
    }

    private void executeMathSafely(Transaction transaction) {
        try {
            boolean isFxTransfer = !transaction.getSourceCurrency().equals(transaction.getDestinationCurrency());

            if (isFxTransfer) {
                log.info("Executing FX Transfer: {} {} -> {} {}",
                        transaction.getAmount(), transaction.getSourceCurrency(),
                        transaction.getDestinationAmount(), transaction.getDestinationCurrency());

                ledgerService.executeFxTransferMath(
                        transaction.getSourceAccountId(), transaction.getSourceCurrency(), transaction.getAmount(),
                        transaction.getDestinationAccountId(), transaction.getDestinationCurrency(), transaction.getDestinationAmount()
                );
            } else {
                log.info("Executing Same-Currency Transfer: {} {}", transaction.getAmount(), transaction.getSourceCurrency());

                ledgerService.executeTransferMath(
                        transaction.getSourceAccountId(), transaction.getDestinationAccountId(),
                        transaction.getAmount(), transaction.getSourceCurrency()
                );
            }
        } catch (LedgerNotInitializedException e) {
            log.warn("Transfer failed: {}", e.getMessage());
            throw TransactionErrors.TransferErrors.invalidTarget(
                    "Transfer failed: One or both wallets do not exist in the ledger."
            );
        }
    }

    private TransactionResponse persistAndDispatch(Transaction transaction) {
        transaction.setStatus(TransactionStatus.SUCCESS);
        Transaction saved = transactionRepository.saveAndFlush(transaction);

        eventPublisher.publish(TransactionMapper.toTransferSuccessEvent(saved));
        log.info("Transfer {} successful.", saved.getReferenceNumber());

        return TransactionMapper.toResponse(saved);
    }
}