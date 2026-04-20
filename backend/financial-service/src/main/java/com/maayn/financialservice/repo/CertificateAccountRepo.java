package com.maayn.financialservice.repo;

import com.maayn.financialservice.entity.CertificateAccountDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateAccountRepo extends MongoRepository<CertificateAccountDocument, UUID> {
    Optional<CertificateAccountDocument> findByCertificateNumber(String certificateNumber);
}
