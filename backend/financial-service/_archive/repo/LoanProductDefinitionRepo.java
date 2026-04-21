package com.maayn.financialservice.repo;

import com.maayn.financialservice.entity.LoanProductDefinitionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanProductDefinitionRepo extends MongoRepository<LoanProductDefinitionDocument, UUID> {
    Optional<LoanProductDefinitionDocument> findByCodeAndActiveTrue(String code);
}
