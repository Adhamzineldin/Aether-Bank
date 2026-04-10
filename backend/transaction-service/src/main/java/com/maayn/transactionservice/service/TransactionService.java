package com.maayn.transactionservice.service;

import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.events.TransactionEventPublisher;
import com.maayn.transactionservice.exceptions.LedgerNotInitializedException;
import com.maayn.transactionservice.handlers.TransferIdempotencyHandler;
import com.maayn.transactionservice.mappers.TransactionMapper;
import com.maayn.transactionservice.repository.TransactionRepository;
import com.maayn.transactionservice.validators.TransactionValidator;
import maayn.veld.generated.errors.TransactionErrors;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.transaction.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import maayn.veld.generated.services.ITransactionService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;
    private final TransactionEventPublisher eventPublisher;
    private final TransactionValidator validator;
    private final TransferIdempotencyHandler idempotencyHandler;

    @Transactional
    @Override
    public TransactionResponse transfer(TransferRequest request) throws Exception {

        Optional<TransactionResponse> cached = idempotencyHandler.getIfAlreadyProcessed(request.getIdempotencyKey());
        if (cached.isPresent()) return cached.get();
        
        Transaction transaction = initializeAndValidate(request);
        
        executeMathSafely(transaction);
        
        return persistAndDispatch(transaction);
    }

    @Transactional(readOnly = true)
    @Override
    public PaginatedTransactionResponse getAccountTransactions(String accountId, getAccountTransactionsRequest input) throws Exception {
        log.info("Fetching transaction history for account {}", accountId);

        Pageable pageable = PageRequest.of(input.getPage(), input.getPageSize());
        UUID accountIdAsUUID = UUID.fromString(accountId);

        Page<Transaction> transactionPage = transactionRepository
                .findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(accountIdAsUUID, accountIdAsUUID, pageable);

        return TransactionMapper.toPaginatedResponse(transactionPage);
    }


    private void executeMathSafely(Transaction transaction) {
        try {
            ledgerService.executeTransferMath(
                    transaction.getSourceAccountId(),
                    transaction.getDestinationAccountId(),
                    transaction.getAmount()
            );
        } catch (LedgerNotInitializedException e) {
            log.warn("Transfer failed: {}", e.getMessage());
            throw TransactionErrors.TransferErrors.invalidTarget(
                    "Transfer failed: Account Ledger not initialized."
            );
        }
    }
    
    
    private Transaction initializeAndValidate(TransferRequest request) {
        Transaction transaction = TransactionMapper.toEntity(request);
        transaction.setIdempotencyKey(request.getIdempotencyKey());
        validator.validateTransfer(transaction);
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