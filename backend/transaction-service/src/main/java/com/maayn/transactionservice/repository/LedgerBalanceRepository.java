package com.maayn.transactionservice.repository;

import com.maayn.transactionservice.entity.LedgerBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LedgerBalanceRepository extends JpaRepository<LedgerBalance, UUID> {
    
    Optional<LedgerBalance> getLedgerBalanceByAccountId(UUID accountId);
}