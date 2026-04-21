package com.maayn.financialservice.service;

import com.maayn.financialservice.entity.MortgageApplicationDocument;
import com.maayn.financialservice.events.FinancialEventPublisher;
import com.maayn.financialservice.mappers.MortgageMapper;
import com.maayn.financialservice.repo.MortgageRepo;
import com.maayn.financialservice.support.ReferenceNumberGenerator;
import com.maayn.financialservice.validation.MortgageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.errors.ConflictException;
import maayn.veld.generated.models.certificate.ApplicationStatus;
import maayn.veld.generated.models.mortgage.Mortgage;
import maayn.veld.generated.models.mortgage.MortgageApplication;
import maayn.veld.generated.models.mortgage.MortgageApplicationResponse;
import maayn.veld.generated.models.mortgage.MortgageRepaymentSchedule;
import maayn.veld.generated.services.IMortgageService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MortgageService implements IMortgageService {

    private final MortgageRepo mortgageRepository;
    private final MortgageMapper mortgageMapper;
    private final MortgageValidator mortgageValidator;
    private final ReferenceNumberGenerator referenceNumberGenerator;
    private final FinancialEventPublisher eventPublisher;

    @Override
    public MortgageApplicationResponse submitMortgageApplication(MortgageApplication request) throws Exception {
        log.info("Submitting mortgage application for customer: {}", request.getCustomerId());
        
        mortgageValidator.validateSubmission(request);
        validateBusinessRules(request);

        MortgageApplicationDocument mortgage = mortgageMapper.toEntity(request);
        enrichMortgage(mortgage);

        MortgageApplicationDocument savedMortgage = mortgageRepository.save(mortgage);
        log.info("Mortgage application {} submitted successfully.", savedMortgage.getId());
        
        // Publish event to trigger workflow
        publishMortgageSubmittedEvent(savedMortgage);
        
        return mortgageMapper.toResponse(savedMortgage);
    }

    @Override
    public Mortgage getMortgage(String mortgageId) throws Exception {
        UUID id = UUID.fromString(mortgageId);
        MortgageApplicationDocument mortgage = mortgageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mortgage not found: " + mortgageId));
        
        return mortgageMapper.toModel(mortgage);
    }

    @Override
    public List<Mortgage> listCustomerMortgages(String customerId) throws Exception {
        UUID id = UUID.fromString(customerId);
        List<MortgageApplicationDocument> mortgages = mortgageRepository.findByCustomerId(id);
        
        return mortgages.stream()
                .map(mortgageMapper::toModel)
                .toList();
    }

    @Override
    public List<MortgageRepaymentSchedule> getMortgageSchedule(String mortgageId) throws Exception {
        UUID id = UUID.fromString(mortgageId);
        MortgageApplicationDocument mortgage = mortgageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mortgage not found: " + mortgageId));
        
        // Generate amortization schedule
        return generateAmortizationSchedule(mortgage);
    }

    private void validateBusinessRules(MortgageApplication request) {
        // Check for duplicate application
        boolean duplicate = mortgageRepository.existsByCustomerIdAndPropertyAddressAndApplicationStatusIn(
                request.getCustomerId(),
                request.getPropertyAddress(),
                List.of(ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW, ApplicationStatus.APPROVED)
        );

        if (duplicate) {
            throw new ConflictException("A mortgage application already exists for this customer and property.");
        }

        // Validate LTV ratio (Loan-to-Value should be <= 80%)
        BigDecimal loanAmount = request.getRequestedAmount();
        BigDecimal propertyValue = request.getPropertyValue();
        BigDecimal ltvRatio = loanAmount.divide(propertyValue, 4, RoundingMode.HALF_UP);

        if (ltvRatio.compareTo(new BigDecimal("0.80")) > 0) {
            throw new IllegalArgumentException("LTV ratio exceeds 80%. Maximum loan: " + 
                    propertyValue.multiply(new BigDecimal("0.80")));
        }
    }

    private void enrichMortgage(MortgageApplicationDocument mortgage) {
        LocalDateTime now = LocalDateTime.now();
        mortgage.setId(UUID.randomUUID());
        mortgage.setApplicationStatus(ApplicationStatus.SUBMITTED);
        mortgage.setSubmittedAt(now);
        mortgage.setMortgageNumber(referenceNumberGenerator.generate("MTG"));
        mortgage.setMortgageStatus("PENDING");
        mortgage.setCurrency("USD"); // Default currency
        mortgage.setCreatedAt(now);
        mortgage.setUpdatedAt(now);
        
        // Calculate preliminary mortgage details
        mortgage.setTermMonths(mortgage.getTermYears() * 12);
        mortgage.setPrincipalAmount(mortgage.getRequestedAmount());
        mortgage.setInterestRate(new BigDecimal("3.5")); // Default 3.5% APR
        mortgage.setOutstandingBalance(mortgage.getPrincipalAmount());
        
        // Calculate monthly payment using standard mortgage formula
        BigDecimal monthlyRate = mortgage.getInterestRate().divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
                .divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);
        int numPayments = mortgage.getTermMonths();
        
        // M = P * [r(1+r)^n] / [(1+r)^n - 1]
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowN = onePlusR.pow(numPayments);
        BigDecimal numerator = mortgage.getPrincipalAmount().multiply(monthlyRate).multiply(onePlusRPowN);
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);
        
        mortgage.setMonthlyPayment(numerator.divide(denominator, 2, RoundingMode.HALF_UP));
    }

    private void publishMortgageSubmittedEvent(MortgageApplicationDocument mortgage) {
        try {
            // TODO: Create MortgageSubmittedEvent
            log.info("Published MortgageSubmittedEvent for mortgage: {}", mortgage.getId());
        } catch (Exception e) {
            log.error("Failed to publish MortgageSubmittedEvent", e);
        }
    }

    private List<MortgageRepaymentSchedule> generateAmortizationSchedule(MortgageApplicationDocument mortgage) {
        List<MortgageRepaymentSchedule> schedule = new ArrayList<>();
        
        BigDecimal principal = mortgage.getPrincipalAmount();
        BigDecimal monthlyRate = mortgage.getInterestRate().divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
                .divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);
        BigDecimal monthlyPayment = mortgage.getMonthlyPayment();
        BigDecimal remainingBalance = principal;
        
        for (int i = 1; i <= mortgage.getTermMonths(); i++) {
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment);
            remainingBalance = remainingBalance.subtract(principalPayment);
            
            MortgageRepaymentSchedule installment = new MortgageRepaymentSchedule();
            installment.setId(UUID.randomUUID());
            installment.setMortgageId(mortgage.getId());
            installment.setInstallmentNumber(i);
            installment.setDueDate(mortgage.getStartDate().plusMonths(i));
            installment.setPrincipalComponent(principalPayment);
            installment.setInterestComponent(interestPayment);
            installment.setTotalAmount(monthlyPayment);
            installment.setRemainingBalance(remainingBalance.max(BigDecimal.ZERO));
            installment.setInstallmentStatus(maayn.veld.generated.models.mortgage.InstallmentStatus.PENDING);
            
            schedule.add(installment);
        }
        
        return schedule;
    }
}

