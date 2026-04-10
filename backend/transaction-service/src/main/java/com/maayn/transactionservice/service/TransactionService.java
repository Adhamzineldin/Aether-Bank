package com.maayn.transactionservice.service;

import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.events.TransactionEventPublisher;
import com.maayn.transactionservice.events.TransferSagaPublisher;
import com.maayn.transactionservice.handlers.TransferIdempotencyHandler;
import com.maayn.transactionservice.mappers.TransactionMapper;
import com.maayn.transactionservice.repository.TransactionRepository;
import com.maayn.transactionservice.validators.TransactionValidator;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.transaction.*;
import maayn.veld.generated.errors.*;
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
    private final TransactionEventPublisher eventPublisher;
    private final TransferSagaPublisher sagaPublisher;
    private final TransactionValidator validator;
    private final TransferIdempotencyHandler idempotencyHandler;

    @Transactional
    @Override
    public TransactionResponse transfer(TransferRequest request) throws Exception {

        Optional<TransactionResponse> cachedResponse = idempotencyHandler.getIfAlreadyProcessed(request.getIdempotencyKey());
        if (cachedResponse.isPresent()) {
            return cachedResponse.get();
        }

        Transaction transaction = TransactionMapper.toEntity(request);
        transaction.setIdempotencyKey(request.getIdempotencyKey());

        validator.validateTransfer(transaction);

        transaction.setStatus(TransactionStatus.PENDING);
        Transaction saved = transactionRepository.saveAndFlush(transaction);

        sagaPublisher.initiateTransferSaga(saved);

        return TransactionMapper.toResponse(saved);
    }
    
    @Transactional(readOnly = true)
    @Override
    public PaginatedTransactionResponse getAccountTransactions(String accountId, getAccountTransactionsRequest input) throws Exception {
        log.info("Fetching transaction history for account {} (Page: {}, Size: {})", accountId, input.getPage(), input.getPageSize());
        
        Pageable pageable = PageRequest.of(input.getPage(), input.getPageSize());

        UUID accountIdAsUUID = UUID.fromString(accountId);
        
        Page<Transaction> transactionPage = transactionRepository
                .findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(accountIdAsUUID, accountIdAsUUID, pageable);

        return TransactionMapper.toPaginatedResponse(transactionPage);
    }


    @Transactional
    public void finalizeTransaction(String referenceNumber, TransactionStatus finalStatus, String reason) {

        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new IllegalStateException("SAGA returned for unknown TXN: " + referenceNumber));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            log.warn("Cannot finalize TXN {}. Expected PENDING but was {}", referenceNumber, transaction.getStatus());
            return;
        }
        transaction.applySagaResult(finalStatus, reason);

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction {} finalized with status {}", referenceNumber, finalStatus);

        dispatchFinalEvent(saved, finalStatus);
    }

    private void dispatchFinalEvent(Transaction transaction, TransactionStatus finalStatus) {
        if (finalStatus == TransactionStatus.SUCCESS) {
            var successEvent = TransactionMapper.toTransferSuccessEvent(transaction);
            eventPublisher.publish(successEvent);
        } else if (finalStatus == TransactionStatus.FAILED) {
            var failedEvent = TransactionMapper.toTransferFailedEvent(transaction, transaction.getFailureReason());
            eventPublisher.publish(failedEvent);
        }
    }

}
