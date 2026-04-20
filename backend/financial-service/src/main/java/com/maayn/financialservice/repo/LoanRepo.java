package com.maayn.financialservice.repo;

import com.maayn.financialservice.entity.LoanApplicationDocument;
import maayn.veld.generated.models.certificate.ApplicationStatus;
import maayn.veld.generated.models.shared.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepo extends MongoRepository<LoanApplicationDocument, UUID> {

    Optional<LoanApplicationDocument> findByIdAndCustomerId(UUID id, UUID customerId);

    List<LoanApplicationDocument> findByCustomerId(UUID customerId);

    Page<LoanApplicationDocument> findByCustomerId(UUID customerId, Pageable pageable);

    List<LoanApplicationDocument> findByProductId(UUID productId);

    List<LoanApplicationDocument> findByApplicationStatus(ApplicationStatus applicationStatus);

    List<LoanApplicationDocument> findByCustomerIdAndApplicationStatus(
            UUID customerId,
            ApplicationStatus applicationStatus
    );

    List<LoanApplicationDocument> findByCustomerIdAndApplicationStatusIn(
            UUID customerId,
            List<ApplicationStatus> statuses
    );

    boolean existsByCustomerIdAndProductIdAndApplicationStatusIn(
            UUID customerId,
            UUID productId,
            List<ApplicationStatus> applicationStatuses
    );

    List<LoanApplicationDocument> findAllByOrderBySubmittedAtDesc();

    List<LoanApplicationDocument> findAll(Sort sort);

    List<LoanApplicationDocument> findByLoanStatus(LoanStatus status);

    void deleteByCustomerId(UUID customerId);
}
