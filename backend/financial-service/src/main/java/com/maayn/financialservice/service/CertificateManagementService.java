package com.maayn.financialservice.service;

import com.maayn.financialservice.domain.certificate.CertificateLifecycleStatus;
import com.maayn.financialservice.domain.certificate.InterestAccrual;
import com.maayn.financialservice.domain.certificate.PayoutLine;
import com.maayn.financialservice.domain.certificate.PayoutMethod;
import com.maayn.financialservice.domain.certificate.PayoutSchedule;
import com.maayn.financialservice.domain.certificate.RateChange;
import com.maayn.financialservice.domain.certificate.WithdrawalResult;
import com.maayn.financialservice.domain.common.FinancialMath;
import com.maayn.financialservice.entity.CertificateAccountDocument;
import com.maayn.financialservice.entity.CertificateProductDefinitionDocument;
import com.maayn.financialservice.entity.PayoutLineDocument;
import com.maayn.financialservice.entity.RateChangeDocument;
import com.maayn.financialservice.exceptions.FinancialOperationException;
import com.maayn.financialservice.gateway.TransactionGateway;
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
    private final TransactionGateway transactionGateway;

    public CertificateAccountDocument issue(CertificateIssueCommand cmd) {
        CertificateProductDefinitionDocument product = loadAndValidateProduct(cmd);
        CertificateAccountDocument account = buildCertificateAccount(cmd, product);
        rebuildPayoutSchedule(account);
        certificateAccountRepo.save(account);
        ledgerPort.recordCertificateEntry(account.getId(), "CERTIFICATE_ISSUED", account.getPrincipal(),
                "CustomerCash", "CertificateLiability", "Certificate issued from product " + product.getCode());
        transactionGateway.lockCertificateFunds(cmd.accountId(), account.getPrincipal(), "EGP", account.getId().toString());
        return account;
    }

    public CertificateAccountDocument deposit(UUID certificateId, BigDecimal amount) {
        CertificateAccountDocument account = load(certificateId);
        BigDecimal normalized = FinancialMath.money(amount);
        account.setPrincipal(FinancialMath.money(account.getPrincipal().add(normalized)));
        account.setUpdatedAt(LocalDateTime.now());
        rebuildPayoutSchedule(account);
        certificateAccountRepo.save(account);
        ledgerPort.recordCertificateEntry(account.getId(), "CERTIFICATE_DEPOSIT", normalized,
                "CustomerCash", "CertificateLiability", "Additional certificate deposit");
        return account;
    }

    public CertificateAccountDocument accrueInterest(UUID certificateId, LocalDate accrualDate) {
        CertificateAccountDocument account = load(certificateId);
        accrueUntil(account, accrualDate == null ? LocalDate.now() : accrualDate);
        certificateAccountRepo.save(account);
        return account;
    }

    public WithdrawalResult withdraw(UUID certificateId, BigDecimal amount, LocalDate withdrawalDate) {
        CertificateAccountDocument account = load(certificateId);
        LocalDate date = withdrawalDate == null ? LocalDate.now() : withdrawalDate;
        accrueUntil(account, date);
        CertificateLiquidityStrategy strategy = strategyRegistry.resolveLiquidityStrategy(account.getLiquidityMethod());
        WithdrawalResult result = strategy.withdraw(account.getId(), account.getPrincipal(),
                account.getAccruedInterest(), FinancialMath.money(amount), account.getPenaltyRate(), date);
        if (!"REJECTED".equals(result.status())) {
            applyWithdrawal(account, amount, result);
        }
        return result;
    }

    public CertificateAccountDocument updateRate(UUID certificateId, RateChange change) {
        CertificateAccountDocument account = load(certificateId);
        account.getRateHistory().add(toDocument(change));
        account.setAnnualRate(change.annualRate());
        account.setUpdatedAt(LocalDateTime.now());
        rebuildPayoutSchedule(account);
        certificateAccountRepo.save(account);
        return account;
    }

    public PayoutSchedule processPayout(UUID certificateId, LocalDate payoutDate) {
        CertificateAccountDocument account = load(certificateId);
        LocalDate date = payoutDate == null ? LocalDate.now() : payoutDate;
        accrueUntil(account, date);
        if (!date.isBefore(account.getMaturityDate())) {
            account.setStatus(CertificateLifecycleStatus.MATURED);
        }
        executePayout(account, date);
        account.setUpdatedAt(LocalDateTime.now());
        rebuildPayoutSchedule(account);
        certificateAccountRepo.save(account);
        return new PayoutSchedule(account.getId(), account.getPayoutLines().stream().map(this::toDomain).toList());
    }

    public PayoutSchedule schedule(UUID certificateId) {
        CertificateAccountDocument account = load(certificateId);
        return new PayoutSchedule(account.getId(), account.getPayoutLines().stream().map(this::toDomain).toList());
    }

    public CertificateAccountDocument get(UUID certificateId) {
        return load(certificateId);
    }

    private CertificateProductDefinitionDocument loadAndValidateProduct(CertificateIssueCommand cmd) {
        CertificateProductDefinitionDocument product = product(cmd.productCode());
        validateRange(cmd.principal(), product.getMinimumPrincipal(), product.getMaximumPrincipal(), "principal");
        return product;
    }

    private CertificateAccountDocument buildCertificateAccount(CertificateIssueCommand cmd, CertificateProductDefinitionDocument product) {
        LocalDate issueDate = cmd.issueDate() == null ? LocalDate.now() : cmd.issueDate();
        LocalDate maturity = cmd.maturityDate() == null ? issueDate.plusDays(product.getTermDays()) : cmd.maturityDate();
        BigDecimal rate = cmd.annualRate() == null ? product.getBaseAnnualRate() : cmd.annualRate();
        CertificateAccountDocument account = buildAccountIdentity(cmd, product, issueDate, maturity);
        buildAccountFinancials(account, cmd.principal(), rate, product);
        return account;
    }

    private CertificateAccountDocument buildAccountIdentity(CertificateIssueCommand cmd, CertificateProductDefinitionDocument product,
                                                              LocalDate issueDate, LocalDate maturity) {
        CertificateAccountDocument account = new CertificateAccountDocument();
        account.setId(UUID.randomUUID());
        account.setCustomerId(cmd.customerId());
        account.setCertificateNumber("CD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        account.setApplicationId(cmd.applicationId());
        account.setProductCode(product.getCode());
        account.setTermDays(product.getTermDays());
        account.setPayoutIntervalDays(product.getPayoutIntervalDays());
        account.setIssueDate(issueDate);
        account.setMaturityDate(maturity);
        account.setLastAccruedDate(issueDate);
        account.setStatus(CertificateLifecycleStatus.ACTIVE);
        account.setPayoutLines(new ArrayList<>());
        account.setRateHistory(new ArrayList<>());
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        return account;
    }

    private void buildAccountFinancials(CertificateAccountDocument account, BigDecimal principal, BigDecimal rate, CertificateProductDefinitionDocument product) {
        account.setPrincipal(FinancialMath.money(principal));
        account.setAccruedInterest(BigDecimal.ZERO.setScale(FinancialMath.SCALE, RoundingMode.HALF_UP));
        account.setAnnualRate(rate);
        account.setInterestMethod(product.getInterestMethod());
        account.setPayoutMethod(product.getPayoutMethod());
        account.setLiquidityMethod(product.getLiquidityMethod());
        account.setRateBehaviorMethod(product.getRateBehaviorMethod());
        account.setPenaltyRate(FinancialMath.money(product.getPenaltyRate()));
    }

    private void applyWithdrawal(CertificateAccountDocument account, BigDecimal amount, WithdrawalResult result) {
        BigDecimal requested = FinancialMath.money(amount);
        BigDecimal penalty = result.penaltyAmount() == null ? BigDecimal.ZERO : result.penaltyAmount();
        BigDecimal net = result.netAmount() == null ? requested.subtract(penalty) : result.netAmount();
        account.setPrincipal(FinancialMath.money(account.getPrincipal().subtract(requested)));
        account.setStatus(account.getPrincipal().compareTo(BigDecimal.ZERO) <= 0
                ? CertificateLifecycleStatus.WITHDRAWN : CertificateLifecycleStatus.PENALIZED);
        account.setUpdatedAt(LocalDateTime.now());
        rebuildPayoutSchedule(account);
        certificateAccountRepo.save(account);
        ledgerPort.recordCertificateEntry(account.getId(), "CERTIFICATE_WITHDRAWAL", net,
                "CertificateLiability", "CustomerCash", "Certificate withdrawal processed");
    }

    private void executePayout(CertificateAccountDocument account, LocalDate date) {
        if (account.getPayoutMethod() == PayoutMethod.PERIODIC) {
            ledgerPort.recordCertificateEntry(account.getId(), "CERTIFICATE_PERIODIC_PAYOUT",
                    account.getAccruedInterest(), "InterestExpense", "CustomerCash", "Periodic certificate payout");
            account.setAccruedInterest(BigDecimal.ZERO.setScale(FinancialMath.SCALE, RoundingMode.HALF_UP));
        } else if (account.getPayoutMethod() == PayoutMethod.AT_MATURITY && !date.isBefore(account.getMaturityDate())) {
            executeMaturityPayout(account);
        }
    }

    private void executeMaturityPayout(CertificateAccountDocument account) {
        BigDecimal payout = account.getPrincipal().add(account.getAccruedInterest());
        ledgerPort.recordCertificateEntry(account.getId(), "CERTIFICATE_MATURITY_PAYOUT", payout,
                "CertificateLiability", "CustomerCash", "Maturity payout");
        account.setPrincipal(BigDecimal.ZERO.setScale(FinancialMath.SCALE, RoundingMode.HALF_UP));
        account.setAccruedInterest(BigDecimal.ZERO.setScale(FinancialMath.SCALE, RoundingMode.HALF_UP));
        account.setStatus(CertificateLifecycleStatus.CLOSED);
    }

    private void rebuildPayoutSchedule(CertificateAccountDocument account) {
        CertificatePayoutStrategy payoutStrategy = strategyRegistry.resolvePayoutStrategy(account.getPayoutMethod());
        CertificateScheduleContext context = buildScheduleContext(account);
        PayoutSchedule schedule = payoutStrategy.buildSchedule(context);
        account.setPayoutLines(schedule.lines().stream().map(this::toDocument).toList());
    }

    private CertificateScheduleContext buildScheduleContext(CertificateAccountDocument account) {
        return new CertificateScheduleContext(
                account.getId(), account.getPrincipal(), account.getAnnualRate(), account.getTermDays(),
                account.getIssueDate(), account.getMaturityDate(), account.getInterestMethod(),
                account.getPayoutMethod(), account.getLiquidityMethod(), account.getRateBehaviorMethod(),
                BigDecimal.valueOf(account.getPayoutIntervalDays() == null ? 365 : account.getPayoutIntervalDays()),
                account.getPenaltyRate(), account.getRateHistory().stream().map(this::toDomain).toList()
        );
    }

    private void accrueUntil(CertificateAccountDocument account, LocalDate untilDate) {
        LocalDate start = account.getLastAccruedDate() == null ? account.getIssueDate() : account.getLastAccruedDate();
        if (!untilDate.isAfter(start)) return;
        CertificateInterestStrategy interest = strategyRegistry.resolveInterestStrategy(account.getInterestMethod());
        RateBehaviorStrategy rate = strategyRegistry.resolveRateBehaviorStrategy(account.getRateBehaviorMethod());
        List<RateChange> history = account.getRateHistory().stream().map(this::toDomain).toList();
        BigDecimal resolvedRate = rate.resolveAnnualRate(account.getAnnualRate(), untilDate, history);
        InterestAccrual accrual = interest.accrue(account.getPrincipal(), resolvedRate, start, untilDate, history);
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
        PayoutLineDocument doc = new PayoutLineDocument();
        doc.setDueDate(line.dueDate());
        doc.setPrincipalReturn(line.principalReturn());
        doc.setInterestAmount(line.interestAmount());
        doc.setTotalAmount(line.totalAmount());
        doc.setStatus(line.status());
        return doc;
    }

    private PayoutLine toDomain(PayoutLineDocument line) {
        return new PayoutLine(line.getDueDate(), line.getPrincipalReturn(),
                line.getInterestAmount(), line.getTotalAmount(), line.getStatus());
    }

    private RateChange toDomain(RateChangeDocument c) {
        return new RateChange(c.getEffectiveDate(), c.getAnnualRate(), c.getSource(), c.getNotes());
    }

    private RateChangeDocument toDocument(RateChange c) {
        RateChangeDocument doc = new RateChangeDocument();
        doc.setEffectiveDate(c.effectiveDate());
        doc.setAnnualRate(c.annualRate());
        doc.setSource(c.source());
        doc.setNotes(c.notes());
        return doc;
    }

    private void validateRange(BigDecimal value, BigDecimal min, BigDecimal max, String field) {
        if (value == null) throw new FinancialOperationException(HttpStatus.BAD_REQUEST, field + " is required");
        if (min != null && value.compareTo(min) < 0)
            throw new FinancialOperationException(HttpStatus.BAD_REQUEST, field + " is below minimum allowed");
        if (max != null && value.compareTo(max) > 0)
            throw new FinancialOperationException(HttpStatus.BAD_REQUEST, field + " is above maximum allowed");
    }
}
