package com.maayn.transactionservice.service;

import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.events.TransactionEventPublisher;
import com.maayn.transactionservice.exceptions.InvalidBalanceException;
import com.maayn.transactionservice.exceptions.LedgerNotInitializedException;
import com.maayn.transactionservice.execution.TransferExecutionService;
import com.maayn.transactionservice.handlers.TransferIdempotencyHandler;
import com.maayn.transactionservice.mappers.TransactionMapper;
import com.maayn.transactionservice.repository.TransactionRepository;
import com.maayn.transactionservice.validators.TransactionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final TransferExecutionService transferExecutionService;

    @Transactional
    @Override
    public TransactionResponse transfer(TransferRequest request) throws Exception {
        Optional<TransactionResponse> cached =
                idempotencyHandler.getIfAlreadyProcessed(request.getIdempotencyKey());

        if (cached.isPresent()) return cached.get();

        Transaction transaction = prepareTransaction(request);

        validator.validateTransfer(transaction);
        
        transferExecutionService.execute(transaction);

        return persistAndDispatch(transaction);
    }

    @Transactional(readOnly = true)
    @Override
    public PaginatedTransactionResponse getAccountTransactions(String accountId, getAccountTransactionsRequest input) {
        log.info("Fetching transaction history for account {} in currency {}", accountId, input.getCurrency());

        UUID accountIdAsUUID = UUID.fromString(accountId);
        Page<Transaction> page = transactionRepository.findByAccountWallet(
                accountIdAsUUID, input.getCurrency(), PageRequest.of(input.getPage(), input.getPageSize())
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

    private TransactionResponse persistAndDispatch(Transaction transaction) {
        transaction.setStatus(TransactionStatus.SUCCESS);
        Transaction saved = transactionRepository.saveAndFlush(transaction);

        eventPublisher.publish(TransactionMapper.toTransferSuccessEvent(saved));
        log.info("Transfer {} successful.", saved.getReferenceNumber());

        return TransactionMapper.toResponse(saved);
    }
}