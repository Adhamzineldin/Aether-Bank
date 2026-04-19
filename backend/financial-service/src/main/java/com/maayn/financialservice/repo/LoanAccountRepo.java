package com.maayn.financialservice.repo;

import com.maayn.financialservice.entity.LoanAccountDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanAccountRepo extends MongoRepository<LoanAccountDocument, UUID> {
    Optional<LoanAccountDocument> findByLoanNumber(String loanNumber);
}
