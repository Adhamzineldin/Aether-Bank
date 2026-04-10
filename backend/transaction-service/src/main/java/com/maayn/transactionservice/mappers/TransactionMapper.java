package com.maayn.transactionservice.mappers;

import com.maayn.transactionservice.entity.Transaction;
import maayn.veld.generated.models.shared.TransactionEvent;
import maayn.veld.generated.models.transaction.TransactionType;
import maayn.veld.generated.models.transaction.TransactionStatus;
import maayn.veld.generated.models.transaction.TransferRequest;
import maayn.veld.generated.sdk.notification.models.shared.TransferFailedEvent;
import maayn.veld.generated.sdk.notification.models.shared.TransferSuccessEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.data.domain.Page;
import maayn.veld.generated.models.transaction.PaginatedTransactionResponse;
import maayn.veld.generated.models.transaction.TransactionResponse;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionMapper {
    
    public static Transaction toEntity(TransferRequest request) {
        return Transaction.builder()
                .sourceAccountId(request.getSourceAccountId())
                .destinationAccountId(request.getDestinationAccountId())
                .amount(request.getAmount())
                .transactionType(request.getType())
                .status(TransactionStatus.PENDING)
                .referenceNumber("TXN-" + UUID.randomUUID().toString().substring(0,8).toUpperCase())
                .currency(request.getCurrency())
                .idempotencyKey(request.getIdempotencyKey())
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
                createdAt
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
    
    public static TransferSuccessEvent toTransferSuccessEvent(Transaction entity) {
        
        return new TransferSuccessEvent(
                entity.getReferenceNumber(),
                entity.getSourceAccountId(),
                entity.getDestinationAccountId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getCreatedAt()
        );
        
    }
    
    public static TransferFailedEvent toTransferFailedEvent(Transaction entity, String failureReason) {
        return new TransferFailedEvent(
                entity.getReferenceNumber(),
                entity.getSourceAccountId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getCreatedAt(),
                failureReason
        );
    }

    public static PaginatedTransactionResponse toPaginatedResponse(Page<Transaction> page) {

        // Convert the raw entities to safe Veld DTOs
        List<TransactionResponse> content = page.getContent().stream()
                .map(TransactionMapper::toResponse)
                .collect(Collectors.toList());

        return new PaginatedTransactionResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
    
}