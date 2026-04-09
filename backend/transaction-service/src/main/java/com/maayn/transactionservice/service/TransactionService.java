package com.maayn.transactionservice.service;

import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.events.TransactionEventPublisher;
import com.maayn.transactionservice.events.TransferSagaPublisher;
import com.maayn.transactionservice.handlers.TransferIdempotencyHandler;
import com.maayn.transactionservice.mappers.TransactionMapper;
import com.maayn.transactionservice.repository.TransactionRepository;
import com.maayn.transactionservice.validators.TransactionValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.models.TransactionEvent;
import maayn.veld.generated.models.TransactionResponse;
import maayn.veld.generated.models.TransactionStatus;
import maayn.veld.generated.models.TransferRequest;
import maayn.veld.generated.errors.*;
import maayn.veld.generated.sdk.iam.IamClient;
import maayn.veld.generated.services.ITransactionService;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
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
        
        TransactionEvent event = TransactionMapper.toEvent(saved);
        eventPublisher.publish(event);
        
        return TransactionMapper.toResponse(saved);
    }
    
}
