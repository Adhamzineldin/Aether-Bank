package com.maayn.financialservice.service;

import com.maayn.financialservice.entity.CertificateApplicationDocument;
import com.maayn.financialservice.gateway.TransactionGateway;
import com.maayn.financialservice.mapper.CertificateMapper;
import com.maayn.financialservice.model.CertificateApplicationRequest;
import com.maayn.financialservice.model.CertificateApplicationResponse;
import com.maayn.financialservice.model.CertificateStatus;
import com.maayn.financialservice.repository.CertificateRepository;
import com.maayn.financialservice.support.ReferenceNumberGenerator;
import com.maayn.financialservice.validator.CertificateValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.loan.ApplicationStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateMapper certificateMapper;
    private final CertificateValidator certificateValidator;
    private final ReferenceNumberGenerator referenceNumberGenerator;
    private final TransactionGateway transactionGateway;

    public CertificateApplicationResponse submit(CertificateApplicationRequest request) {
        certificateValidator.validateSubmission(request);
        CertificateApplicationDocument certificate = certificateMapper.toEntity(request);
        enrich(certificate);
        lockFunds(certificate, request);
        CertificateApplicationDocument saved = certificateRepository.save(certificate);
        log.info("Certificate application {} submitted", saved.getId());
        return certificateMapper.toResponse(saved);
    }

    private void lockFunds(CertificateApplicationDocument certificate, CertificateApplicationRequest request) {
        String idempotencyKey = "cert-lock-" + certificate.getCertificateNumber();
        transactionGateway.lockCertificateFunds(
                request.getAccountId(), request.getPrincipal(), "USD", idempotencyKey);
        certificate.setOpenDate(LocalDateTime.now());
        certificate.setCertificateStatus(CertificateStatus.ACTIVE);
        certificate.setApplicationStatus(ApplicationStatus.APPROVED);
    }

    private void enrich(CertificateApplicationDocument certificate) {
        LocalDateTime now = LocalDateTime.now();
        certificate.setApplicationStatus(ApplicationStatus.SUBMITTED);
        certificate.setSubmittedAt(now);
        certificate.setCertificateNumber(referenceNumberGenerator.generate("CD"));
        certificate.setCertificateStatus(CertificateStatus.PENDING);
        certificate.setCreatedAt(now);
        certificate.setUpdatedAt(now);
    }
}
