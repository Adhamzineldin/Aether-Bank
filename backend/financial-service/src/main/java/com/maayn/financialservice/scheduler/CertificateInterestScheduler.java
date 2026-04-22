package com.maayn.financialservice.scheduler;

import com.maayn.financialservice.entity.CertificateApplicationDocument;
import com.maayn.financialservice.repo.CertificateRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.shared.CertificateStatus;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
import maayn.veld.generated.sdk.transaction.TransactionClient;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import maayn.veld.generated.sdk.transaction.models.transaction.TransferRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CertificateInterestScheduler {

    private final CertificateRepo certificateRepository;
    private final TransactionClient transactionClient;

    /**
     * Runs every day at 3 AM: updates accrued value for still-active certificates using
     * <strong>daily</strong> compounding (365-day year), and settles any certificate whose
     * maturity date is today or in the past. Short-term / “daily-style” products therefore
     * see correct growth between open and maturity, not only on month boundaries.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void dailyCertificateProcessing() {
        log.info("Starting daily certificate accrual and maturity processing...");

        LocalDate today = LocalDate.now();

        List<CertificateApplicationDocument> activeCertificates =
                certificateRepository.findByCertificateStatus(CertificateStatus.ACTIVE);

        log.info("Found {} active certificates", activeCertificates.size());

        for (CertificateApplicationDocument certificate : activeCertificates) {
            try {
                if (certificate.getMaturityDate() != null
                        && !today.isBefore(certificate.getMaturityDate().toLocalDate())) {
                    processCertificateMaturity(certificate);
                } else {
                    refreshCurrentValueDailyCompound(certificate, today);
                }
            } catch (Exception e) {
                log.error("Failed daily certificate processing for {}", certificate.getId(), e);
            }
        }

        log.info("Daily certificate processing completed");
    }

    private void processCertificateMaturity(CertificateApplicationDocument certificate) {
        log.info("Processing maturity for certificate: {}", certificate.getCertificateNumber());

        int termDays = resolveFullTermDays(certificate);
        BigDecimal finalAmount = compoundDaily(
                certificate.getPrincipal(),
                certificate.getInterestRate(),
                termDays);

        // Transfer principal + interest back to customer account
        try {
            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setIdempotencyKey("cert-mature-" + certificate.getId());
            transferRequest.setSourceAccountId(SystemAccounts.CASH_VAULT_ID);
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
            certificate.setCurrentValue(finalAmount);
            certificateRepository.save(certificate);

            log.info("Certificate {} matured. Amount {} credited to account", 
                    certificate.getCertificateNumber(), finalAmount);

        } catch (Exception e) {
            log.error("Failed to credit maturity amount for certificate: {}", certificate.getId(), e);
            throw new RuntimeException("Certificate maturity processing failed", e);
        }
    }

    /**
     * Sets {@code currentValue} to principal grown by daily compounding for the number of
     * full calendar days since {@code openDate} (0 days = principal only).
     */
    private void refreshCurrentValueDailyCompound(CertificateApplicationDocument certificate, LocalDate today) {
        if (certificate.getOpenDate() == null) {
            return;
        }
        LocalDate open = certificate.getOpenDate().toLocalDate();
        long elapsed = ChronoUnit.DAYS.between(open, today);
        if (elapsed < 0) {
            elapsed = 0;
        }
        int days = (int) Math.min(elapsed, Integer.MAX_VALUE - 2);
        BigDecimal value = compoundDaily(
                certificate.getPrincipal(),
                certificate.getInterestRate(),
                days);
        certificate.setCurrentValue(value);
        certificate.setUpdatedAt(LocalDateTime.now());
        certificateRepository.save(certificate);
        log.debug("Certificate {} accrued value {} after {} day(s)", certificate.getCertificateNumber(), value, days);
    }

    /**
     * Maturity / mark-to-market: {@code A = P (1 + r/365)^d} with {@code r} as nominal APR percent.
     */
    private static BigDecimal compoundDaily(BigDecimal principal, BigDecimal annualPercent, int days) {
        if (principal == null || annualPercent == null || days <= 0) {
            return principal != null ? principal.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        }
        BigDecimal dailyRate = annualPercent
                .divide(new BigDecimal("100"), 12, RoundingMode.HALF_UP)
                .divide(new BigDecimal("365"), 12, RoundingMode.HALF_UP);
        BigDecimal factor = BigDecimal.ONE.add(dailyRate).pow(days);
        return principal.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    /** Full term in days: calendar open→maturity when both set, else {@code termDays}, else 365. */
    private static int resolveFullTermDays(CertificateApplicationDocument c) {
        if (c.getOpenDate() != null && c.getMaturityDate() != null) {
            long d = ChronoUnit.DAYS.between(c.getOpenDate().toLocalDate(), c.getMaturityDate().toLocalDate());
            if (d > 0) {
                return (int) Math.min(d, Integer.MAX_VALUE - 2);
            }
        }
        if (c.getTermDays() != null && c.getTermDays() > 0) {
            return c.getTermDays();
        }
        return 365;
    }
}

