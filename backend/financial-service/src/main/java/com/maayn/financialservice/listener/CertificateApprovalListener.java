package com.maayn.financialservice.listener;

import com.maayn.financialservice.audit.AuditPublisher;
import com.maayn.financialservice.entity.CertificateApplicationDocument;
import com.maayn.financialservice.gateway.TransactionGateway;
import com.maayn.financialservice.repo.CertificateRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.certificate.ApplicationStatus;
import maayn.veld.generated.models.shared.CertificateStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CertificateApprovalListener {

    private final CertificateRepo certificateRepository;
    private final TransactionGateway transactionGateway;
    private final AuditPublisher auditPublisher;

    @RabbitListener(queues = "certificate.approved.queue")
    public void onCertificateApproved(Map<String, Object> event) {
        try {
            log.info("Received certificate approval event: {}", event);

            UUID certificateId = UUID.fromString(event.get("certificateId").toString());
            CertificateApplicationDocument cert = certificateRepository.findById(certificateId)
                    .orElseThrow(() -> new RuntimeException("Certificate not found: " + certificateId));

            if (cert.getAccountId() == null) {
                throw new IllegalStateException("Certificate " + certificateId + " has no funding accountId");
            }
            if (cert.getPrincipal() == null || cert.getPrincipal().signum() <= 0) {
                throw new IllegalStateException("Certificate " + certificateId + " has no principal");
            }

            cert.setApplicationStatus(ApplicationStatus.APPROVED);
            cert.setReviewedAt(LocalDateTime.now());
            cert.setUpdatedAt(LocalDateTime.now());
            certificateRepository.save(cert);

            String currency = cert.getCurrency() != null ? cert.getCurrency() : "USD";
            transactionGateway.lockCertificateFunds(
                    cert.getAccountId(),
                    cert.getPrincipal(),
                    currency,
                    "certificate-lock-" + cert.getId());

            int termDays = cert.getTermDays() != null && cert.getTermDays() > 0 ? cert.getTermDays() : 365;
            LocalDateTime now = LocalDateTime.now();
            cert.setCertificateStatus(CertificateStatus.ACTIVE);
            cert.setOpenDate(now);
            cert.setMaturityDate(now.plusDays(termDays));
            cert.setCurrentValue(cert.getPrincipal());
            cert.setUpdatedAt(now);
            certificateRepository.save(cert);

            log.info("Certificate {} funded and activated; matures {}", certificateId, cert.getMaturityDate());
            auditPublisher.publishSuccess(
                    "ACTIVATE_CERTIFICATE",
                    cert.getCustomerId(),
                    String.format("Certificate %s activated: principal=%s %s, matures=%s",
                            certificateId, cert.getPrincipal(), currency, cert.getMaturityDate()));
        } catch (Exception e) {
            log.error("Failed to process certificate approval event", e);
            auditPublisher.publishFailure(
                    "ACTIVATE_CERTIFICATE",
                    null,
                    String.format("Certificate activation failed for event %s: %s", event, e.getMessage()));
        }
    }
}
