package com.maayn.transactionservice.entity;

import jdk.jfr.DataAmount;
import maayn.veld.generated.models.TransactionStatus;
import maayn.veld.generated.models.TransactionType;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "source_account_id")
    private UUID sourceAccountId;

    @Column(name = "destination_account_id")
    private UUID destinationAccountId;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
    
    @Column(name = "reference_number", unique = true, nullable = false)
    private String referenceNumber;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
}
