package com.maayn.financialservice.service;

import com.maayn.financialservice.entity.LoanApplicationDocument;
import com.maayn.financialservice.mapper.LoanMapper;
import com.maayn.financialservice.repository.LoanRepository;
import com.maayn.financialservice.support.ReferenceNumberGenerator;
import com.maayn.financialservice.validator.LoanValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.errors.BadRequestException;
import maayn.veld.generated.errors.ConflictException;
import maayn.veld.generated.errors.NotFoundException;
import maayn.veld.generated.models.loan.*;
import maayn.veld.generated.services.ILoanService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService implements ILoanService {

    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final LoanValidator loanValidator;
    private final ReferenceNumberGenerator referenceNumberGenerator;

    @Override
    public LoanApplicationResponse loanSubmit(LoanApplication request) throws Exception {
        loanValidator.validateSubmission(request);
        checkDuplicate(request);
        LoanApplicationDocument loan = loanMapper.toEntity(request);
        enrich(loan);
        LoanApplicationDocument saved = loanRepository.save(loan);
        log.info("Loan application {} submitted", saved.getId());
        return loanMapper.toResponse(saved);
    }

    @Override
    public LoanApplication getLoan(String id) throws Exception {
        return loanRepository.findById(parseUuid(id))
                .map(loanMapper::toModel)
                .orElseThrow(() -> new NotFoundException("Loan application not found: " + id));
    }

    @Override
    public List<LoanApplication> getCustomerLoans(String customerId) throws Exception {
        return loanRepository.findByCustomerId(parseUuid(customerId))
                .stream().map(loanMapper::toModel).toList();
    }

    @Override
    public LoanApplicationResponse cancelLoanApplication(String id) throws Exception {
        LoanApplicationDocument loan = loanRepository.findById(parseUuid(id))
                .orElseThrow(() -> new NotFoundException("Loan application not found: " + id));
        if (loan.getApplicationStatus() != ApplicationStatus.SUBMITTED)
            throw new BadRequestException("Only SUBMITTED applications can be cancelled");
        loan.setApplicationStatus(ApplicationStatus.CANCELLED);
        loan.setUpdatedAt(LocalDateTime.now());
        return loanMapper.toResponse(loanRepository.save(loan));
    }

    @Override
    public Loan getApprovedLoan(String loanId) throws Exception {
        return loanRepository.findById(parseUuid(loanId))
                .filter(l -> l.getLoanStatus() != null && l.getLoanStatus() != LoanStatus.PENDING)
                .map(loanMapper::toLoan)
                .orElseThrow(() -> new NotFoundException("Approved loan not found: " + loanId));
    }

    @Override
    public List<Loan> getCustomerApprovedLoans(String customerId) throws Exception {
        return loanRepository.findByCustomerIdAndLoanStatusIn(parseUuid(customerId), approvedStatuses())
                .stream().map(loanMapper::toLoan).toList();
    }

    @Override
    public List<Loan> getAllLoans() throws Exception {
        return loanRepository.findByLoanStatusIn(approvedStatuses())
                .stream().map(loanMapper::toLoan).toList();
    }

    @Override
    public List<RepaymentSchedule> getRepaymentSchedule(String loanId) throws Exception {
        return List.of();
    }

    @Override
    public List<LoanPayment> getLoanPayments(String loanId) throws Exception {
        return List.of();
    }

    @Override
    public Collateral addCollateral(String loanId, Collateral input) throws Exception {
        return input;
    }

    @Override
    public List<Collateral> getLoanCollaterals(String loanId) throws Exception {
        return List.of();
    }

    @Override
    public LoanDocument uploadLoanDocument(String id, LoanDocument input) throws Exception {
        return input;
    }

    @Override
    public List<LoanDocument> getLoanDocuments(String id) throws Exception {
        return List.of();
    }

    @Override
    public List<LoanApproval> getLoanApprovals(String id) throws Exception {
        return List.of();
    }

    private void checkDuplicate(LoanApplication request) {
        boolean exists = loanRepository.existsByCustomerIdAndProductIdAndApplicationStatusIn(
                request.getCustomerId(), request.getProductId(),
                List.of(ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW, ApplicationStatus.APPROVED));
        if (exists) throw new ConflictException("A loan application already exists for this customer and product.");
    }

    private void enrich(LoanApplicationDocument loan) {
        LocalDateTime now = LocalDateTime.now();
        loan.setApplicationStatus(ApplicationStatus.SUBMITTED);
        loan.setSubmittedAt(now);
        loan.setLoanNumber(referenceNumberGenerator.generate("LN"));
        loan.setLoanStatus(LoanStatus.PENDING);
        loan.setCreatedAt(now);
        loan.setUpdatedAt(now);
    }

    private static UUID parseUuid(String id) {
        try { return UUID.fromString(id); }
        catch (IllegalArgumentException ex) { throw new BadRequestException("Invalid ID: " + id); }
    }

    private static List<LoanStatus> approvedStatuses() {
        return List.of(LoanStatus.APPROVED, LoanStatus.DISBURSED, LoanStatus.ACTIVE,
                LoanStatus.OVERDUE, LoanStatus.DEFAULTED, LoanStatus.CLOSED);
    }
}
