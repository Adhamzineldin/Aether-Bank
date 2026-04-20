package com.maayn.financialservice.repository;

import com.maayn.financialservice.entity.LoanApplicationDocument;
import maayn.veld.generated.models.loan.ApplicationStatus;
import maayn.veld.generated.models.loan.LoanStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends MongoRepository<LoanApplicationDocument, UUID> {

    List<LoanApplicationDocument> findByCustomerId(UUID customerId);

    List<LoanApplicationDocument> findByProductId(UUID productId);

    List<LoanApplicationDocument> findByApplicationStatus(ApplicationStatus status);

    List<LoanApplicationDocument> findByCustomerIdAndLoanStatusIn(UUID customerId, List<LoanStatus> statuses);

    List<LoanApplicationDocument> findByLoanStatusIn(List<LoanStatus> statuses);

    boolean existsByCustomerIdAndProductIdAndApplicationStatusIn(
            UUID customerId, UUID productId, List<ApplicationStatus> statuses);

    void deleteByCustomerId(UUID customerId);
}
