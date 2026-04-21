package com.maayn.financialservice.scheduler;

import com.maayn.financialservice.entity.CertificateApplicationDocument;
import com.maayn.financialservice.repo.CertificateRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.shared.CertificateStatus;
import maayn.veld.generated.sdk.transaction.TransactionClient;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import maayn.veld.generated.sdk.transaction.models.transaction.TransferRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CertificateInterestScheduler {

    private final CertificateRepo certificateRepository;
    private final TransactionClient transactionClient;

    // Bank's certificate deposit account
    private static final UUID CERTIFICATE_VAULT_ACCOUNT = UUID.fromString("33333333-3333-3333-3333-333333333333");

    /**
     * Runs every day at 3 AM to check for mature certificates and credit interest
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void processCertificateMaturity() {
        log.info("Starting certificate maturity processing...");

        LocalDate today = LocalDate.now();
        
        // Find all active certificates
        List<CertificateApplicationDocument> activeCertificates = 
            certificateRepository.findByCertificateStatus(CertificateStatus.ACTIVE);

        log.info("Found {} active certificates to check", activeCertificates.size());

        for (CertificateApplicationDocument certificate : activeCertificates) {
            try {
                if (certificate.getMaturityDate() != null && 
                    !today.isBefore(certificate.getMaturityDate().toLocalDate())) {
                    processCertificateMaturity(certificate);
                }
            } catch (Exception e) {
                log.error("Failed to process certificate maturity: {}", certificate.getId(), e);
            }
        }

        log.info("Certificate maturity processing completed");
    }

    /**
     * Runs monthly on 1st day at 4 AM to add compound interest for active certificates
     */
    @Scheduled(cron = "0 0 4 1 * *")
    public void addMonthlyCompoundInterest() {
        log.info("Starting monthly compound interest calculation for certificates...");

        List<CertificateApplicationDocument> activeCertificates = 
            certificateRepository.findByCertificateStatus(CertificateStatus.ACTIVE);

        for (CertificateApplicationDocument certificate : activeCertificates) {
            try {
                addCompoundInterest(certificate);
            } catch (Exception e) {
                log.error("Failed to add interest to certificate: {}", certificate.getId(), e);
            }
        }

        log.info("Monthly compound interest calculation completed");
    }

    private void processCertificateMaturity(CertificateApplicationDocument certificate) {
        log.info("Processing maturity for certificate: {}", certificate.getCertificateNumber());

        // Calculate final amount with compound interest
        BigDecimal finalAmount = calculateMaturityAmount(certificate);

        // Transfer principal + interest back to customer account
        try {
            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setIdempotencyKey("cert-mature-" + certificate.getId());
            transferRequest.setSourceAccountId(CERTIFICATE_VAULT_ACCOUNT);
            transferRequest.setDestinationAccountId(certificate.getAccountId());
            transferRequest.setAmount(finalAmount);
            transferRequest.setCurrency(certificate.getCurrency());
            transferRequest.setSourceCurrency(certificate.getCurrency());
            transferRequest.setDestinationCurrency(certificate.getCurrency());
            transferRequest.setType(TransactionType.INTERNAL_TRANSFER);

            transactionClient.transaction.transfer(transferRequest);

            // Update certificate status
            certificate.setCertificateStatus(CertificateStatus.MATURED);
            certificate.setMaturedAmount(finalAmount);
            certificateRepository.save(certificate);

            log.info("Certificate {} matured. Amount {} credited to account", 
                    certificate.getCertificateNumber(), finalAmount);

        } catch (Exception e) {
            log.error("Failed to credit maturity amount for certificate: {}", certificate.getId(), e);
            throw new RuntimeException("Certificate maturity processing failed", e);
        }
    }

    private void addCompoundInterest(CertificateApplicationDocument certificate) {
        if (certificate.getCurrentValue() == null) {
            certificate.setCurrentValue(certificate.getPrincipal());
        }

        // Calculate monthly interest (compound)
        BigDecimal monthlyRate = certificate.getInterestRate()
                .divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
                .divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);

        BigDecimal interest = certificate.getCurrentValue()
                .multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);

        // Add interest to current value (compound)
        BigDecimal newValue = certificate.getCurrentValue().add(interest);
        certificate.setCurrentValue(newValue);
        
        certificateRepository.save(certificate);

        log.info("Added compound interest {} to certificate: {}. New value: {}", 
                interest, certificate.getCertificateNumber(), newValue);
    }

    private BigDecimal calculateMaturityAmount(CertificateApplicationDocument certificate) {
        // Calculate compound interest over entire tenure
        // A = P(1 + r/n)^(nt)
        // Where: P = principal, r = annual rate, n = 12 (monthly compounding), t = years

        BigDecimal principal = certificate.getPrincipal();
        BigDecimal annualRate = certificate.getInterestRate().divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        int months = Math.max(1, certificate.getTermDays() / 30);
        
        // Monthly rate
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);
        
        // Calculate compound interest
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal compoundFactor = onePlusRate.pow(months);
        
        BigDecimal maturityAmount = principal.multiply(compoundFactor)
                .setScale(2, RoundingMode.HALF_UP);

        return maturityAmount;
    }
}

