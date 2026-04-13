package com.maayn.transactionservice.mappers;

import com.maayn.transactionservice.entity.Transaction;
import maayn.veld.generated.models.transaction.TransactionStatus;
import maayn.veld.generated.models.transaction.TransactionType;
import maayn.veld.generated.models.transaction.TransferRequest;
import maayn.veld.generated.sdk.notification.models.shared.TransferFailedEvent;
import maayn.veld.generated.sdk.notification.models.shared.TransferSuccessEvent;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import maayn.veld.generated.models.transaction.PaginatedTransactionResponse;
import maayn.veld.generated.models.transaction.TransactionResponse;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionMapper {
    
    public static Transaction toEntity(TransferRequest request) {
        String sourceCurrency = request.getSourceCurrency() != null ? request.getSourceCurrency() : request.getCurrency();
        String destinationCurrency = request.getDestinationCurrency() != null ? request.getDestinationCurrency() : request.getCurrency();
        // currency defaults to sourceCurrency if not explicitly provided
        String currency = request.getCurrency() != null ? request.getCurrency() : sourceCurrency;

        return Transaction.builder()
                .sourceAccountId(request.getSourceAccountId())
                .destinationAccountId(request.getDestinationAccountId())
                .amount(request.getAmount())
                .transactionType(request.getType() != null ? request.getType() : TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .referenceNumber("TXN-" + UUID.randomUUID().toString().substring(0,8).toUpperCase())
                .currency(currency)
                .sourceCurrency(sourceCurrency)
                .destinationCurrency(destinationCurrency)
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