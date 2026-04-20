package com.maayn.accountservice.repository;

import com.maayn.accountservice.entity.BankAccount;
import com.maayn.accountservice.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {
    
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    
    List<BankAccount> findByCustomerId(UUID customerId);
    
    List<BankAccount> findByCustomerIdAndStatus(UUID customerId, AccountStatus status);
    
    boolean existsByAccountNumber(String accountNumber);
    
    long countByCustomerIdAndStatus(UUID customerId, AccountStatus status);
}

