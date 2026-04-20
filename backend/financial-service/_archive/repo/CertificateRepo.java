package com.maayn.financialservice.repo;

import com.maayn.financialservice.entity.CertificateApplicationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CertificateRepo extends MongoRepository<CertificateApplicationDocument, UUID> {
}
