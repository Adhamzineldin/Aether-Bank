package com.maayn.financialservice.repo;

import com.maayn.financialservice.entity.MortgageApplicationDocument;
import maayn.veld.generated.models.certificate.ApplicationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MortgageRepo extends MongoRepository<MortgageApplicationDocument, UUID> {

    boolean existsByCustomerIdAndPropertyAddressIgnoreCaseAndApplicationStatusIn(
            UUID customerId,
            String propertyAddress,
            List<ApplicationStatus> applicationStatuses
    );
}
