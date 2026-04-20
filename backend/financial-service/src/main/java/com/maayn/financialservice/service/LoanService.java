package com.maayn.financialservice.service;

import com.maayn.financialservice.entity.LoanApplicationDocument;
import com.maayn.financialservice.events.FinancialEventPublisher;
import com.maayn.financialservice.mappers.LoanMapper;
import com.maayn.financialservice.repo.LoanRepo;
import com.maayn.financialservice.support.ReferenceNumberGenerator;
import com.maayn.financialservice.validation.LoanValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.errors.ConflictException;
import maayn.veld.generated.models.certificate.ApplicationStatus;
import maayn.veld.generated.models.loan.LoanApplication;
import maayn.veld.generated.models.loan.LoanApplicationResponse;
import maayn.veld.generated.models.shared.LoanStatus;
import maayn.veld.generated.services.ILoanService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService implements ILoanService {

    private final LoanRepo loanRepository;
    private final LoanMapper loanMapper;
    private final LoanValidator loanValidator;
    private final ReferenceNumberGenerator referenceNumberGenerator;
    private final FinancialEventPublisher eventPublisher;

    @Override
    public LoanApplicationResponse loanSubmit(LoanApplication request) {
        loanValidator.validateSubmission(request);
        validateBusinessRules(request);

        LoanApplicationDocument loan = loanMapper.toEntity(request);
        enrichLoan(loan);

        LoanApplicationDocument savedLoan = loanRepository.save(loan);
        log.info("Loan application {} submitted successfully.", savedLoan.getId());
        
        // Publish event to trigger workflow
        eventPublisher.publishLoanSubmitted(
            savedLoan.getId(), 
            savedLoan.getCustomerId(), 
            savedLoan.getRequestedAmount()
        );
        
        return loanMapper.toResponse(savedLoan);
    }

    private void validateBusinessRules(LoanApplication request) {
        boolean duplicate = loanRepository.existsByCustomerIdAndProductIdAndApplicationStatusIn(
                request.getCustomerId(),
                request.getProductId(),
                List.of(ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW, ApplicationStatus.APPROVED)
        );

        if (duplicate) {
            throw new ConflictException("A loan application already exists for this customer and product.");
        }
    }

    private void enrichLoan(LoanApplicationDocument loan) {
        LocalDateTime now = LocalDateTime.now();
        loan.setApplicationStatus(ApplicationStatus.SUBMITTED);
        loan.setSubmittedAt(now);
        loan.setLoanNumber(referenceNumberGenerator.generate("LN"));
        loan.setLoanStatus(LoanStatus.PENDING);
        loan.setCreatedAt(now);
        loan.setUpdatedAt(now);
    }
}
