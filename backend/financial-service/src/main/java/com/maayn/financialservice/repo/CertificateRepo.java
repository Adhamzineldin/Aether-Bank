package com.maayn.financialservice.repo;

import com.maayn.financialservice.entity.CertificateApplicationDocument;
import maayn.veld.generated.models.certificate.ApplicationStatus;
import maayn.veld.generated.models.shared.CertificateStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CertificateRepo extends MongoRepository<CertificateApplicationDocument, UUID> {
    boolean existsByCustomerIdAndProductIdAndApplicationStatusIn(
        UUID customerId,
        UUID productId,
        List<ApplicationStatus> statuses
    );

    List<CertificateApplicationDocument> findByCertificateStatus(CertificateStatus status);

    List<CertificateApplicationDocument> findByCustomerId(UUID customerId);
}
