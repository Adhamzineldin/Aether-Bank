package com.maayn.financialservice.service;

import com.maayn.financialservice.entity.CertificateApplicationDocument;
import com.maayn.financialservice.events.FinancialEventPublisher;
import com.maayn.financialservice.mappers.CertificateMapper;
import com.maayn.financialservice.repo.CertificateRepo;
import com.maayn.financialservice.support.ReferenceNumberGenerator;
import com.maayn.financialservice.validation.CertificateValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.certificate.ApplicationStatus;
import maayn.veld.generated.models.certificate.CertificateApplication;
import maayn.veld.generated.models.certificate.CertificateApplicationResponse;
import maayn.veld.generated.models.shared.CertificateStatus;
import maayn.veld.generated.services.ICertificateService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService implements ICertificateService {

    private final CertificateRepo certificateRepository;
    private final CertificateMapper certificateMapper;
    private final CertificateValidator certificateValidator;
    private final ReferenceNumberGenerator referenceNumberGenerator;
    private final FinancialEventPublisher eventPublisher;

    @Override
    public CertificateApplicationResponse certificateSubmit(CertificateApplication request) {
        certificateValidator.validateSubmission(request);

        CertificateApplicationDocument certificate = certificateMapper.toEntity(request);
        enrichCertificate(certificate);

        CertificateApplicationDocument savedCertificate = certificateRepository.save(certificate);
        log.info("Certificate application {} submitted successfully.", savedCertificate.getId());

        eventPublisher.publishCertificateSubmitted(
                savedCertificate.getId(),
                savedCertificate.getCustomerId(),
                savedCertificate.getPrincipal());

        return certificateMapper.toResponse(savedCertificate);
    }

    private void enrichCertificate(CertificateApplicationDocument certificate) {
        LocalDateTime now = LocalDateTime.now();
        certificate.setApplicationStatus(ApplicationStatus.SUBMITTED);
        certificate.setSubmittedAt(now);
        certificate.setCertificateNumber(referenceNumberGenerator.generate("CD"));
        certificate.setCertificateStatus(CertificateStatus.PENDING);
        certificate.setCreatedAt(now);
        certificate.setUpdatedAt(now);
    }
}
