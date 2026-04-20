package com.maayn.financialservice.repository;

import com.maayn.financialservice.entity.MortgageApplicationDocument;
import maayn.veld.generated.models.loan.ApplicationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MortgageRepository extends MongoRepository<MortgageApplicationDocument, UUID> {

    List<MortgageApplicationDocument> findByCustomerId(UUID customerId);

    boolean existsByCustomerIdAndPropertyAddressIgnoreCaseAndApplicationStatusIn(
            UUID customerId, String propertyAddress, List<ApplicationStatus> statuses);
}
