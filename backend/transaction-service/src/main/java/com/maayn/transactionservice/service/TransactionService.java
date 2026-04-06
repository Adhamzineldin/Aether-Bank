package com.maayn.transactionservice.service;

import com.maayn.transactionservice.entity.Transaction;
import com.maayn.transactionservice.mappers.TransactionMapper;
import com.maayn.transactionservice.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.models.TransactionResponse;
import maayn.veld.generated.models.TransferRequest;
import maayn.veld.generated.errors.*;
import maayn.veld.generated.services.ITransactionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    @Override
    public TransactionResponse transfer(TransferRequest request) {
        Transaction transaction = TransactionMapper.toEntity(request);

        if (transaction.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw TransactionErrors.TransferErrors.insufficientFunds("test");
        }
        
        // 2. Logic (e.g., Check balance, Fraud check)
        
        // Ensure DB-generated fields (like createdAt) are available before mapping.
        Transaction saved = transactionRepository.saveAndFlush(transaction);
        
        return TransactionMapper.toResponse(saved);
    }
}
