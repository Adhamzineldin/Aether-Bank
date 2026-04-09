package com.maayn.transactionservice.repository;


import com.maayn.transactionservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySourceAccountId(UUID sourceAccountId);
    
    List<Transaction> findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(UUID source, UUID dest);
    
    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    
}
