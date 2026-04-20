package com.maayn.financialservice.service;

import com.maayn.financialservice.entity.MortgageApplicationDocument;
import com.maayn.financialservice.mapper.MortgageMapper;
import com.maayn.financialservice.model.MortgageApplicationRequest;
import com.maayn.financialservice.model.MortgageApplicationResponse;
import com.maayn.financialservice.model.MortgageStatus;
import com.maayn.financialservice.repository.MortgageRepository;
import com.maayn.financialservice.support.ReferenceNumberGenerator;
import com.maayn.financialservice.validator.MortgageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.errors.ConflictException;
import maayn.veld.generated.models.loan.ApplicationStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MortgageService {

    private final MortgageRepository mortgageRepository;
    private final MortgageMapper mortgageMapper;
    private final MortgageValidator mortgageValidator;
    private final ReferenceNumberGenerator referenceNumberGenerator;

    public MortgageApplicationResponse submit(MortgageApplicationRequest request) {
        mortgageValidator.validateSubmission(request);
        checkDuplicate(request);
        MortgageApplicationDocument mortgage = mortgageMapper.toEntity(request);
        enrich(mortgage);
        MortgageApplicationDocument saved = mortgageRepository.save(mortgage);
        log.info("Mortgage application {} submitted", saved.getId());
        return mortgageMapper.toResponse(saved);
    }

    private void checkDuplicate(MortgageApplicationRequest request) {
        boolean exists = mortgageRepository.existsByCustomerIdAndPropertyAddressIgnoreCaseAndApplicationStatusIn(
                request.getCustomerId(), request.getPropertyAddress(),
                List.of(ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW, ApplicationStatus.APPROVED));
        if (exists)
            throw new ConflictException("A mortgage application already exists for this customer and property.");
    }

    private void enrich(MortgageApplicationDocument mortgage) {
        LocalDateTime now = LocalDateTime.now();
        mortgage.setApplicationStatus(ApplicationStatus.SUBMITTED);
        mortgage.setSubmittedAt(now);
        mortgage.setMortgageNumber(referenceNumberGenerator.generate("MTG"));
        mortgage.setMortgageStatus(MortgageStatus.PENDING);
        mortgage.setCreatedAt(now);
        mortgage.setUpdatedAt(now);
    }
}
