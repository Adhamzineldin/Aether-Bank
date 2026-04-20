package com.maayn.financialservice.repository;

import com.maayn.financialservice.entity.CertificateApplicationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CertificateRepository extends MongoRepository<CertificateApplicationDocument, UUID> {
}
