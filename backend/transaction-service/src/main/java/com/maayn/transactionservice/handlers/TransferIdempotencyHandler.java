package com.maayn.transactionservice.handlers;

import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.mappers.TransactionMapper;
import com.maayn.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.transaction.TransactionResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferIdempotencyHandler {

    private final TransactionRepository repository;
    
    public Optional<TransactionResponse> getIfAlreadyProcessed(String idempotencyKey) {

        Optional<Transaction> existingTransaction = repository.findByIdempotencyKey(idempotencyKey);

        if (existingTransaction.isPresent()) {
            log.info("Idempotent request caught. Returning existing transaction for key: {}", idempotencyKey);
            return Optional.of(TransactionMapper.toResponse(existingTransaction.get()));
        }

        return Optional.empty();
    }
}