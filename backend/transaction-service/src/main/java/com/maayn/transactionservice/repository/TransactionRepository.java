package com.maayn.transactionservice.repository;


import com.maayn.transactionservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            SELECT t FROM Transaction t
            WHERE (t.sourceAccountId = :accountId AND t.sourceCurrency = :currency)
               OR (t.destinationAccountId = :accountId AND t.destinationCurrency = :currency)
            ORDER BY t.createdAt DESC
            """)
    Page<Transaction> findByAccountWallet(
            @Param("accountId") UUID accountId,
            @Param("currency") String currency,
            Pageable pageable
    );
}
