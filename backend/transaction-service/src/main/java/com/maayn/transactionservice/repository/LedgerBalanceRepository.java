package com.maayn.transactionservice.repository;

import com.maayn.transactionservice.entity.LedgerAccountId;
import com.maayn.transactionservice.entity.LedgerBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerBalanceRepository extends JpaRepository<LedgerBalance, LedgerAccountId> {
}