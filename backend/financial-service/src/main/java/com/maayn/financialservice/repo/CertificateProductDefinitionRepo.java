package com.maayn.financialservice.repo;

import com.maayn.financialservice.entity.CertificateProductDefinitionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateProductDefinitionRepo extends MongoRepository<CertificateProductDefinitionDocument, UUID> {
    Optional<CertificateProductDefinitionDocument> findByCodeAndActiveTrue(String code);
}
