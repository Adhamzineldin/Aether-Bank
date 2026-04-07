package com.maayn.transactionservice.mappers;

import com.maayn.transactionservice.entity.Transaction;
import maayn.veld.generated.models.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TransactionMapper {
    
    public static Transaction toEntity(TransferRequest request) {
        return Transaction.builder()
                .sourceAccountId(request.sourceAccountId())
                .destinationAccountId(request.destinationAccountId())
                .amount(request.amount())
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .referenceNumber("TXN-" + UUID.randomUUID().toString().substring(0,8).toUpperCase())
                .build();
    }

    public static TransactionResponse toResponse(Transaction entity) {
        LocalDateTime createdAt = entity.getCreatedAt() != null
                ? entity.getCreatedAt()
                : LocalDateTime.now();

        return new TransactionResponse(
                entity.getReferenceNumber(),
                entity.getAmount(),
                entity.getStatus(),
                createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
    
    public static TransactionEvent toEvent(Transaction entity) {
        return new TransactionEvent(
                entity.getReferenceNumber(),
                entity.getSourceAccountId(),
                entity.getDestinationAccountId(),
                entity.getAmount(),
                entity.getStatus()
        );
    }
}