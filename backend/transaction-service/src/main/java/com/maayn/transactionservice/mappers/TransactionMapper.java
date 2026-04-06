package com.maayn.transactionservice.mappers;

import com.maayn.transactionservice.entity.Transaction;
import maayn.veld.generated.models.TransactionResponse;
import maayn.veld.generated.models.TransactionStatus;
import maayn.veld.generated.models.TransactionType;
import maayn.veld.generated.models.TransferRequest;

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
        return new TransactionResponse(
                entity.getReferenceNumber(),
                entity.getAmount(),
                entity.getStatus(),
                entity.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}