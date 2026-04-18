package com.maayn.financialservice.service;

import com.maayn.financialservice.entity.Loan;
import com.maayn.financialservice.exceptions.LoanException;
import com.maayn.financialservice.mappers.LoanMapper;
import com.maayn.financialservice.repo.LoanRepo;
import com.maayn.financialservice.validation.LoanValidator;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.models.loan.ApplicationStatus;
import maayn.veld.generated.models.loan.LoanApplication;
import maayn.veld.generated.models.loan.LoanApplicationResponse;
import maayn.veld.generated.services.ILoanService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoanService implements ILoanService {

    private final LoanRepo loanRepository;
    private final LoanMapper loanMapper;
    private final LoanValidator loanValidator;

    public LoanApplicationResponse loanSubmit(LoanApplication request) {

        // 🔴 1. Validate request (structure + values)
        loanValidator.validateSubmission(request);

        // 🔴 2. Business rule validation
        validateBusinessRules(request);

        // 🔴 3. Map to entity
        Loan loan = loanMapper.toEntity(request);

        // 🔴 4. Set system-controlled fields
        enrichLoan(loan);

        // 🔴 5. Persist
        Loan savedLoan = loanRepository.save(loan);

        // 🔴 6. (Future) Emit event → Notification / Workflow
        // eventPublisher.publish(new LoanSubmittedEvent(savedLoan));

        // 🔴 7. Return response
        return loanMapper.toResponse(savedLoan);
    }

    /**
     * Business rules (NOT basic validation)
     */
    private void validateBusinessRules(LoanApplication request) {

        boolean exists = loanRepository.existsByCustomerIdAndProductId(
                request.getCustomerId(),
                request.getProductId()
        );
        throw new LoanException("Duplicate loan", HttpStatus.CONFLICT);
    }

    /**
     * System-managed fields
     */
    private void enrichLoan(Loan loan) {
        loan.setApplicationStatus(ApplicationStatus.SUBMITTED);
        loan.setSubmittedAt(LocalDateTime.now());
    }
}