package com.maayn.transactionservice.service;

import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.mappers.TransactionMapper;
import com.maayn.transactionservice.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.models.TransactionResponse;
import maayn.veld.generated.models.TransactionStatus;
import maayn.veld.generated.models.TransactionType;
import maayn.veld.generated.models.TransferRequest;
import maayn.veld.generated.errors.TransferException.*;
import maayn.veld.generated.services.ITransactionService;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    @Override
    public TransactionResponse transfer(TransferRequest request) {
        Transaction transaction = TransactionMapper.toEntity(request);
        
        // 2. Logic (e.g., Check balance, Fraud check)
        
        Transaction saved = transactionRepository.save(transaction);
        
        return TransactionMapper.toResponse(saved);
    }
}
