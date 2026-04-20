package com.maayn.financialservice.service;

import com.maayn.financialservice.entity.MortgageApplicationDocument;
import com.maayn.financialservice.mappers.MortgageMapper;
import com.maayn.financialservice.repo.MortgageRepo;
import com.maayn.financialservice.support.ReferenceNumberGenerator;
import com.maayn.financialservice.validation.MortgageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.errors.ConflictException;
import maayn.veld.generated.models.certificate.ApplicationStatus;
import maayn.veld.generated.models.mortgage.MortgageApplication;
import maayn.veld.generated.models.mortgage.MortgageApplicationResponse;
import maayn.veld.generated.models.shared.MortgageStatus;
import maayn.veld.generated.services.IMortgageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MortgageService implements IMortgageService {

    private final MortgageRepo mortgageRepository;
    private final MortgageMapper mortgageMapper;
    private final MortgageValidator mortgageValidator;
    private final ReferenceNumberGenerator referenceNumberGenerator;

    @Transactional
    @Override
    public MortgageApplicationResponse mortgageSubmit(MortgageApplication request) {
        mortgageValidator.validateSubmission(request);
        validateBusinessRules(request);

        MortgageApplicationDocument mortgage = mortgageMapper.toEntity(request);
        enrichMortgage(mortgage);

        MortgageApplicationDocument savedMortgage = mortgageRepository.save(mortgage);
        log.info("Mortgage application {} submitted successfully.", savedMortgage.getId());
        return mortgageMapper.toResponse(savedMortgage);
    }

    private void validateBusinessRules(MortgageApplication request) {
        boolean duplicate = mortgageRepository.existsByCustomerIdAndPropertyAddressIgnoreCaseAndApplicationStatusIn(
                request.getCustomerId(),
                request.getPropertyAddress(),
                List.of(ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW, ApplicationStatus.APPROVED)
        );

        if (duplicate) {
            throw new ConflictException("A mortgage application already exists for this customer and property.");
        }
    }

    private void enrichMortgage(MortgageApplicationDocument mortgage) {
        LocalDateTime now = LocalDateTime.now();
        mortgage.setApplicationStatus(ApplicationStatus.SUBMITTED);
        mortgage.setSubmittedAt(now);
        mortgage.setMortgageNumber(referenceNumberGenerator.generate("MTG"));
        mortgage.setMortgageStatus(MortgageStatus.PENDING);
        mortgage.setCreatedAt(now);
        mortgage.setUpdatedAt(now);
    }
}
