package com.maayn.financialservice.repo;

import com.maayn.financialservice.entity.Loan;
import maayn.veld.generated.models.loan.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepo extends MongoRepository<Loan, String> {

    Optional<Loan> findByIdAndCustomerId(String id, UUID customerId);

    List<Loan> findByCustomerId(UUID customerId);

    Page<Loan> findByCustomerId(UUID customerId, Pageable pageable);

    List<Loan> findByProductId(UUID productId);

    List<Loan> findByApplicationStatus(ApplicationStatus applicationStatus);

    List<Loan> findByCustomerIdAndApplicationStatus(
            UUID customerId,
            ApplicationStatus applicationStatus
    );

    List<Loan> findByCustomerIdAndApplicationStatusIn(
            UUID customerId,
            List<ApplicationStatus> statuses
    );

    boolean existsByCustomerIdAndProductId(
            UUID customerId,
            UUID productId
    );

    List<Loan> findAllByOrderBySubmittedAtDesc();

    List<Loan> findAll(Sort sort);

    void deleteByCustomerId(UUID customerId);
}