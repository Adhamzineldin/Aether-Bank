package com.maayn.financialservice.service;

import com.maayn.financialservice.domain.certificate.CertificateInterestMethod;
import com.maayn.financialservice.domain.certificate.CertificateLifecycleStatus;
import com.maayn.financialservice.domain.certificate.Deposit;
import com.maayn.financialservice.domain.certificate.InterestAccrual;
import com.maayn.financialservice.domain.certificate.PayoutLine;
import com.maayn.financialservice.domain.certificate.PayoutMethod;
import com.maayn.financialservice.domain.certificate.PayoutSchedule;
import com.maayn.financialservice.domain.certificate.RateBehaviorMethod;
import com.maayn.financialservice.domain.certificate.RateChange;
import com.maayn.financialservice.domain.certificate.WithdrawalResult;
import com.maayn.financialservice.domain.common.FinancialMath;
import com.maayn.financialservice.entity.CertificateAccountDocument;
import com.maayn.financialservice.entity.CertificateProductDefinitionDocument;
import com.maayn.financialservice.entity.PayoutLineDocument;
import com.maayn.financialservice.entity.RateChangeDocument;
import com.maayn.financialservice.exceptions.FinancialOperationException;
import com.maayn.financialservice.repo.CertificateAccountRepo;
import com.maayn.financialservice.repo.CertificateProductDefinitionRepo;
import com.maayn.financialservice.strategy.certificate.CertificateInterestStrategy;
import com.maayn.financialservice.strategy.certificate.CertificateLiquidityStrategy;
import com.maayn.financialservice.strategy.certificate.CertificatePayoutStrategy;
import com.maayn.financialservice.strategy.certificate.CertificateScheduleContext;
import com.maayn.financialservice.strategy.certificate.RateBehaviorStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificateManagementService {

    private final CertificateAccountRepo certificateAccountRepo;
    private final CertificateProductDefinitionRepo certificateProductDefinitionRepo;
    private final CertificateStrategyRegistry strategyRegistry;
    private final LedgerPort ledgerPort;

    @Transactional
    public CertificateAccountDocument issue(CertificateIssueCommand request) {
        CertificateProductDefinitionDocument product = product(request.productCode());
        validateRange(request.principal(), product.getMinimumPrincipal(), product.getMaximumPrincipal(), "principal");

        LocalDate issueDate = request.issueDate() == null ? LocalDate.now() : request.issueDate();
        LocalDate maturityDate = request.maturityDate() == null ? issueDate.plusDays(product.getTermDays()) : request.maturityDate();
        BigDecimal annualRate = request.annualRate() == null ? product.getBaseAnnualRate() : request.annualRate();

        CertificateAccountDocument account = new CertificateAccountDocument();
        account.setId(UUID.randomUUID());
        account.setCustomerId(request.customerId());
        account.setCertificateNumber("CD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        account.setApplicationId(request.applicationId());
        account.setProductCode(product.getCode());
        account.setPrincipal(FinancialMath.money(request.principal()));
        account.setAccruedInterest(BigDecimal.ZERO.setScale(FinancialMath.SCALE, RoundingMode.HALF_UP));
        account.setAnnualRate(annualRate);
        account.setInterestMethod(product.getInterestMethod());
        account.setPayoutMethod(product.getPayoutMethod());
        account.setLiquidityMethod(product.getLiquidityMethod());
        account.setRateBehaviorMethod(product.getRateBehaviorMethod());
        account.setPenaltyRate(FinancialMath.money(product.getPenaltyRate()));
        account.setTermDays(product.getTermDays());
        account.setPayoutIntervalDays(product.getPayoutIntervalDays());
        account.setIssueDate(issueDate);
        account.setMaturityDate(maturityDate);
        account.setLastAccruedDate(issueDate);
        account.setStatus(CertificateLifecycleStatus.ACTIVE);
        account.setPayoutLines(new ArrayList<>());
        account.setRateHistory(new ArrayList<>());
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        rebuildPayoutSchedule(account);
        certificateAccountRepo.save(account);
        ledgerPort.recordCertificateEntry(account.getId(), "CERTIFICATE_ISSUED", account.getPrincipal(), "CustomerCash", "CertificateLiability", "Certificate issued from product " + product.getCode());
        return account;
    }

    @Transactional
    public CertificateAccountDocument deposit(UUID certificateId, BigDecimal amount) {
        CertificateAccountDocument account = load(certificateId);
        BigDecimal normalized = FinancialMath.money(amount);
        account.setPrincipal(FinancialMath.money(account.getPrincipal().add(normalized)));
        account.setUpdatedAt(LocalDateTime.now());
        rebuildPayoutSchedule(account);
        certificateAccountRepo.save(account);
        ledgerPort.recordCertificateEntry(account.getId(), "CERTIFICATE_DEPOSIT", normalized, "CustomerCash", "CertificateLiability", "Additional certificate deposit");
        return account;
    }

    @Transactional
    public CertificateAccountDocument accrueInterest(UUID certificateId, LocalDate accrualDate) {
        CertificateAccountDocument account = load(certificateId);
        accrueUntil(account, accrualDate == null ? LocalDate.now() : accrualDate);
        certificateAccountRepo.save(account);
        return account;
    }

    @Transactional
    public WithdrawalResult withdraw(UUID certificateId, BigDecimal amount, LocalDate withdrawalDate) {
        CertificateAccountDocument account = load(certificateId);
        LocalDate resolvedDate = withdrawalDate == null ? LocalDate.now() : withdrawalDate;
        accrueUntil(account, resolvedDate);
        CertificateLiquidityStrategy strategy = strategyRegistry.resolveLiquidityStrategy(account.getLiquidityMethod());
        WithdrawalResult result = strategy.withdraw(account.getId(), account.getPrincipal(), account.getAccruedInterest(), FinancialMath.money(amount), account.getPenaltyRate(), resolvedDate);
        if (!"REJECTED".equals(result.status())) {
            BigDecimal requested = FinancialMath.money(amount);
            BigDecimal penalty = result.penaltyAmount() == null ? BigDecimal.ZERO : result.penaltyAmount();
            BigDecimal net = result.netAmount() == null ? requested.subtract(penalty) : result.netAmount();
            account.setPrincipal(FinancialMath.money(account.getPrincipal().subtract(requested)));
            if (account.getPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
                account.setStatus(CertificateLifecycleStatus.WITHDRAWN);
            } else {
                account.setStatus(CertificateLifecycleStatus.PENALIZED);
            }
            account.setUpdatedAt(LocalDateTime.now());
            rebuildPayoutSchedule(account);
            certificateAccountRepo.save(account);
            ledgerPort.recordCertificateEntry(account.getId(), "CERTIFICATE_WITHDRAWAL", net, "CertificateLiability", "CustomerCash", "Certificate withdrawal processed");
        }
        return result;
    }

    @Transactional
    public CertificateAccountDocument updateRate(UUID certificateId, RateChange change) {
        CertificateAccountDocument account = load(certificateId);
        RateChangeDocument rateChange = new RateChangeDocument();
        rateChange.setEffectiveDate(change.effectiveDate());
        rateChange.setAnnualRate(change.annualRate());
        rateChange.setSource(change.source());
        rateChange.setNotes(change.notes());
        account.getRateHistory().add(rateChange);
        account.setAnnualRate(change.annualRate());
        account.setUpdatedAt(LocalDateTime.now());
        rebuildPayoutSchedule(account);
        certificateAccountRepo.save(account);
        return account;
    }

    @Transactional
    public PayoutSchedule processPayout(UUID certificateId, LocalDate payoutDate) {
        CertificateAccountDocument account = load(certificateId);
        LocalDate date = payoutDate == null ? LocalDate.now() : payoutDate;
        accrueUntil(account, date);
        if (!date.isBefore(account.getMaturityDate())) {
            account.setStatus(CertificateLifecycleStatus.MATURED);
        }
        List<PayoutLine> lines = account.getPayoutLines().stream().map(this::toDomain).toList();
        if (account.getPayoutMethod() == PayoutMethod.PERIODIC) {
            ledgerPort.recordCertificateEntry(account.getId(), "CERTIFICATE_PERIODIC_PAYOUT", account.getAccruedInterest(), "InterestExpense", "CustomerCash", "Periodic certificate payout");
            account.setAccruedInterest(BigDecimal.ZERO.setScale(FinancialMath.SCALE, RoundingMode.HALF_UP));
        } else if (account.getPayoutMethod() == PayoutMethod.AT_MATURITY && !date.isBefore(account.getMaturityDate())) {
            BigDecimal payout = account.getPrincipal().add(account.getAccruedInterest());
            ledgerPort.recordCertificateEntry(account.getId(), "CERTIFICATE_MATURITY_PAYOUT", payout, "CertificateLiability", "CustomerCash", "Maturity payout");
            account.setPrincipal(BigDecimal.ZERO.setScale(FinancialMath.SCALE, RoundingMode.HALF_UP));
            account.setAccruedInterest(BigDecimal.ZERO.setScale(FinancialMath.SCALE, RoundingMode.HALF_UP));
            account.setStatus(CertificateLifecycleStatus.CLOSED);
        }
        account.setUpdatedAt(LocalDateTime.now());
        rebuildPayoutSchedule(account);
        certificateAccountRepo.save(account);
        return new PayoutSchedule(account.getId(), lines);
    }

    @Transactional(readOnly = true)
    public PayoutSchedule schedule(UUID certificateId) {
        CertificateAccountDocument account = load(certificateId);
        return new PayoutSchedule(account.getId(), account.getPayoutLines().stream().map(this::toDomain).toList());
    }

    @Transactional(readOnly = true)
    public CertificateAccountDocument get(UUID certificateId) {
        return load(certificateId);
    }

    private void rebuildPayoutSchedule(CertificateAccountDocument account) {
        CertificatePayoutStrategy payoutStrategy = strategyRegistry.resolvePayoutStrategy(account.getPayoutMethod());
        CertificateScheduleContext context = new CertificateScheduleContext(
                account.getId(),
                account.getPrincipal(),
                account.getAnnualRate(),
                account.getTermDays(),
                account.getIssueDate(),
                account.getMaturityDate(),
                account.getInterestMethod(),
                account.getPayoutMethod(),
                account.getLiquidityMethod(),
                account.getRateBehaviorMethod(),
                BigDecimal.valueOf(account.getPayoutIntervalDays() == null ? 365 : account.getPayoutIntervalDays()),
                account.getPenaltyRate(),
                account.getRateHistory().stream().map(this::toDomain).toList()
        );
        PayoutSchedule schedule = payoutStrategy.buildSchedule(context);
        account.setPayoutLines(schedule.lines().stream().map(this::toDocument).toList());
    }

    private void accrueUntil(CertificateAccountDocument account, LocalDate untilDate) {
        LocalDate start = account.getLastAccruedDate() == null ? account.getIssueDate() : account.getLastAccruedDate();
        if (!untilDate.isAfter(start)) {
            return;
        }
        CertificateInterestStrategy interestStrategy = strategyRegistry.resolveInterestStrategy(account.getInterestMethod());
        RateBehaviorStrategy rateStrategy = strategyRegistry.resolveRateBehaviorStrategy(account.getRateBehaviorMethod());
        BigDecimal resolvedRate = rateStrategy.resolveAnnualRate(account.getAnnualRate(), untilDate, account.getRateHistory().stream().map(this::toDomain).toList());
        InterestAccrual accrual = interestStrategy.accrue(account.getPrincipal(), resolvedRate, start, untilDate, account.getRateHistory().stream().map(this::toDomain).toList());
        account.setAccruedInterest(FinancialMath.money(account.getAccruedInterest().add(accrual.interestAmount())));
        account.setLastAccruedDate(untilDate);
    }

    private CertificateAccountDocument load(UUID certificateId) {
        return certificateAccountRepo.findById(certificateId)
                .orElseThrow(() -> new FinancialOperationException(HttpStatus.NOT_FOUND, "Certificate account not found"));
    }

    private CertificateProductDefinitionDocument product(String code) {
        return certificateProductDefinitionRepo.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new FinancialOperationException(HttpStatus.NOT_FOUND, "Certificate product not found: " + code));
    }

    private PayoutLineDocument toDocument(PayoutLine line) {
        PayoutLineDocument document = new PayoutLineDocument();
        document.setDueDate(line.dueDate());
        document.setPrincipalReturn(line.principalReturn());
        document.setInterestAmount(line.interestAmount());
        document.setTotalAmount(line.totalAmount());
        document.setStatus(line.status());
        return document;
    }

    private PayoutLine toDomain(PayoutLineDocument line) {
        return new PayoutLine(
                line.getDueDate(),
                line.getPrincipalReturn(),
                line.getInterestAmount(),
                line.getTotalAmount(),
                line.getStatus()
        );
    }

    private RateChange toDomain(RateChangeDocument change) {
        return new RateChange(change.getEffectiveDate(), change.getAnnualRate(), change.getSource(), change.getNotes());
    }

    private void validateRange(BigDecimal value, BigDecimal min, BigDecimal max, String field) {
        if (value == null) {
            throw new FinancialOperationException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        if (min != null && value.compareTo(min) < 0) {
            throw new FinancialOperationException(HttpStatus.BAD_REQUEST, field + " is below minimum allowed");
        }
        if (max != null && value.compareTo(max) > 0) {
            throw new FinancialOperationException(HttpStatus.BAD_REQUEST, field + " is above maximum allowed");
        }
    }
}

record CertificateIssueCommand(
        UUID customerId,
        UUID applicationId,
        String productCode,
        BigDecimal principal,
        BigDecimal annualRate,
        LocalDate issueDate,
        LocalDate maturityDate
) {
}
