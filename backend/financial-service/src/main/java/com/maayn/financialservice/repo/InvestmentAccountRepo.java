package com.maayn.financialservice.repo;

import com.maayn.financialservice.entity.InvestmentAccountDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestmentAccountRepo extends MongoRepository<InvestmentAccountDocument, UUID> {
    
    List<InvestmentAccountDocument> findByCustomerId(UUID customerId);
    
    Optional<InvestmentAccountDocument> findByAccountNumber(String accountNumber);
}

