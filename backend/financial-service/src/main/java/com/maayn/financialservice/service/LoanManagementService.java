package com.maayn.financialservice.service;

import com.maayn.financialservice.domain.common.FinancialMath;
import com.maayn.financialservice.domain.loan.InterestDetail;
import com.maayn.financialservice.domain.loan.LoanLifecycleStatus;
import com.maayn.financialservice.domain.loan.Payment;
import com.maayn.financialservice.domain.loan.RateChange;
import com.maayn.financialservice.domain.loan.RepaymentSchedule;
import com.maayn.financialservice.domain.loan.RepaymentScheduleLine;
import com.maayn.financialservice.entity.LoanAccountDocument;
import com.maayn.financialservice.entity.LoanProductDefinitionDocument;
import com.maayn.financialservice.entity.LoanScheduleLineDocument;
import com.maayn.financialservice.entity.RateChangeDocument;
import com.maayn.financialservice.exceptions.FinancialOperationException;
import com.maayn.financialservice.gateway.TransactionGateway;
import com.maayn.financialservice.repo.LoanAccountRepo;
import com.maayn.financialservice.repo.LoanProductDefinitionRepo;
import com.maayn.financialservice.strategy.loan.LoanInterestStrategy;
import com.maayn.financialservice.strategy.loan.LoanRateStrategy;
import com.maayn.financialservice.strategy.loan.LoanRepaymentStrategy;
import com.maayn.financialservice.strategy.loan.LoanScheduleContext;
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
public class LoanManagementService {

    private final LoanAccountRepo loanAccountRepo;
    private final LoanProductDefinitionRepo loanProductDefinitionRepo;
    private final LoanStrategyRegistry strategyRegistry;
    private final LedgerPort ledgerPort;
    private final TransactionGateway transactionGateway;

    public LoanAccountDocument originate(LoanOriginationCommand cmd) {
        LoanProductDefinitionDocument product = loadAndValidateProduct(cmd);
        LoanAccountDocument account = buildLoanAccount(cmd, product);
        rebuildSchedule(account);
        loanAccountRepo.save(account);
        ledgerPort.recordLoanEntry(account.getId(), "LOAN_ORIGINATED", account.getPrincipal(),
                "LoanReceivable", "CustomerCash", "Loan originated from product " + product.getCode());
        transactionGateway.disburseLoan(cmd.accountId(), account.getPrincipal(), "EGP", account.getId().toString());
        return account;
    }

    public LoanAccountDocument accrueInterest(UUID loanId, LocalDate accrualDate) {
        LoanAccountDocument account = load(loanId);
        accrueUntil(account, accrualDate == null ? LocalDate.now() : accrualDate);
        loanAccountRepo.save(account);
        return account;
    }

    public LoanAccountDocument processPayment(UUID loanId, Payment payment) {
        LoanAccountDocument account = load(loanId);
        LocalDate paymentDate = payment.paymentDate() == null ? LocalDate.now() : payment.paymentDate().toLocalDate();
        accrueUntil(account, paymentDate);
        applyLatePenaltyIfOverdue(account, payment, paymentDate);
        BigDecimal amount = FinancialMath.money(payment.amount());
        allocatePayment(account, amount);
        closeIfFullyPaid(account);
        account.setNextDueDate(paymentDate.plusMonths(1));
        account.setUpdatedAt(LocalDateTime.now());
        rebuildSchedule(account);
        loanAccountRepo.save(account);
        ledgerPort.recordLoanEntry(account.getId(), "LOAN_PAYMENT", amount, "CustomerCash",
                "LoanReceivable", "Loan payment" + (payment.reference() == null ? "" : " ref=" + payment.reference()));
        return account;
    }

    public LoanAccountDocument earlyRepayment(UUID loanId, BigDecimal amount, LocalDate repaymentDate) {
        LoanAccountDocument account = load(loanId);
        LocalDate date = repaymentDate == null ? LocalDate.now() : repaymentDate;
        accrueUntil(account, date);
        BigDecimal requested = FinancialMath.money(amount);
        applyEarlyRepayment(account, requested);
        rebuildSchedule(account);
        account.setUpdatedAt(LocalDateTime.now());
        loanAccountRepo.save(account);
        ledgerPort.recordLoanEntry(account.getId(), "LOAN_EARLY_REPAYMENT", requested,
                "CustomerCash", "LoanReceivable", "Early repayment with penalty");
        return account;
    }

    public LoanAccountDocument applyLatePenalty(UUID loanId, BigDecimal overdueAmount, LocalDate dueDate, LocalDate paidDate) {
        LoanAccountDocument account = load(loanId);
        BigDecimal penalty = FinancialMath.money(overdueAmount.multiply(account.getPenaltyRate()));
        account.setAccruedInterest(FinancialMath.money(account.getAccruedInterest().add(penalty)));
        LocalDate resolvedPaidDate = paidDate == null ? LocalDate.now() : paidDate;
        if (dueDate != null && resolvedPaidDate.isAfter(dueDate)) {
            account.setStatus(LoanLifecycleStatus.DELINQUENT);
        }
        account.setUpdatedAt(LocalDateTime.now());
        loanAccountRepo.save(account);
        ledgerPort.recordLoanEntry(account.getId(), "LOAN_LATE_PENALTY", penalty,
                "PenaltyReceivable", "PenaltyIncome", "Late payment penalty assessed");
        return account;
    }

    public LoanAccountDocument updateRate(UUID loanId, RateChange change) {
        LoanAccountDocument account = load(loanId);
        account.getRateHistory().add(toDocument(change));
        account.setAnnualRate(change.annualRate());
        rebuildSchedule(account);
        account.setUpdatedAt(LocalDateTime.now());
        loanAccountRepo.save(account);
        return account;
    }

    public RepaymentSchedule schedule(UUID loanId) {
        LoanAccountDocument account = load(loanId);
        return new RepaymentSchedule(account.getId(), account.getScheduleLines().stream().map(this::toDomain).toList());
    }

    public LoanAccountDocument get(UUID loanId) {
        return load(loanId);
    }

    private LoanProductDefinitionDocument loadAndValidateProduct(LoanOriginationCommand cmd) {
        LoanProductDefinitionDocument product = product(cmd.productCode());
        validateRange(cmd.principal(), product.getMinimumPrincipal(), product.getMaximumPrincipal(), "principal");
        validateTenure(cmd.tenureMonths(), product.getMinimumTenureMonths(), product.getMaximumTenureMonths());
        return product;
    }

    private LoanAccountDocument buildLoanAccount(LoanOriginationCommand cmd, LoanProductDefinitionDocument product) {
        LocalDate startDate = cmd.startDate() == null ? LocalDate.now() : cmd.startDate();
        Integer tenure = cmd.tenureMonths() == null ? product.getDefaultTenureMonths() : cmd.tenureMonths();
        LocalDate maturity = cmd.maturityDate() == null ? startDate.plusMonths(tenure) : cmd.maturityDate();
        LoanAccountDocument account = buildAccountIdentity(cmd, product, startDate, tenure, maturity);
        buildAccountFinancials(account, cmd, product);
        return account;
    }

    private LoanAccountDocument buildAccountIdentity(LoanOriginationCommand cmd, LoanProductDefinitionDocument product,
                                                       LocalDate startDate, Integer tenure, LocalDate maturity) {
        LoanAccountDocument account = new LoanAccountDocument();
        account.setId(UUID.randomUUID());
        account.setCustomerId(cmd.customerId());
        account.setLoanNumber("LN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        account.setApplicationId(cmd.applicationId());
        account.setProductCode(product.getCode());
        account.setLoanType(product.getLoanType());
        account.setTenureMonths(tenure);
        account.setStartDate(startDate);
        account.setMaturityDate(maturity);
        account.setNextDueDate(startDate.plusMonths(1));
        account.setStatus(LoanLifecycleStatus.ACTIVE);
        account.setScheduleLines(new ArrayList<>());
        account.setRateHistory(new ArrayList<>());
        account.setLastAccruedDate(startDate);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        return account;
    }

    private void buildAccountFinancials(LoanAccountDocument account, LoanOriginationCommand cmd, LoanProductDefinitionDocument product) {
        account.setPrincipal(FinancialMath.money(cmd.principal()));
        account.setOutstandingPrincipal(FinancialMath.money(cmd.principal()));
        account.setAccruedInterest(BigDecimal.ZERO.setScale(FinancialMath.SCALE, RoundingMode.HALF_UP));
        account.setAnnualRate(product.getBaseAnnualRate());
        account.setInterestMethod(product.getInterestMethod());
        account.setRateMode(product.getRateMode());
        account.setRepaymentMethod(product.getRepaymentMethod());
        account.setMonthlyFee(FinancialMath.money(product.getMonthlyFee()));
        account.setPenaltyRate(FinancialMath.money(product.getPenaltyRate()));
    }

    private void applyLatePenaltyIfOverdue(LoanAccountDocument account, Payment payment, LocalDate paymentDate) {
        if (account.getNextDueDate() != null && paymentDate.isAfter(account.getNextDueDate())) {
            BigDecimal penalty = FinancialMath.money(FinancialMath.money(payment.amount()).multiply(account.getPenaltyRate()));
            account.setAccruedInterest(FinancialMath.money(account.getAccruedInterest().add(penalty)));
        }
    }

    private void allocatePayment(LoanAccountDocument account, BigDecimal amount) {
        BigDecimal remaining = amount;
        BigDecimal interestPaid = min(remaining, account.getAccruedInterest());
        remaining = remaining.subtract(interestPaid);
        account.setAccruedInterest(FinancialMath.money(account.getAccruedInterest().subtract(interestPaid)));
        BigDecimal principalPaid = min(remaining, account.getOutstandingPrincipal());
        account.setOutstandingPrincipal(FinancialMath.money(account.getOutstandingPrincipal().subtract(principalPaid)));
    }

    private void closeIfFullyPaid(LoanAccountDocument account) {
        if (account.getOutstandingPrincipal().compareTo(BigDecimal.ZERO) <= 0
                && account.getAccruedInterest().compareTo(BigDecimal.ZERO) <= 0) {
            account.setStatus(LoanLifecycleStatus.CLOSED);
        }
    }

    private void applyEarlyRepayment(LoanAccountDocument account, BigDecimal requested) {
        BigDecimal penalty = FinancialMath.money(requested.multiply(account.getPenaltyRate()));
        BigDecimal net = requested.subtract(penalty);
        BigDecimal principalPaid = min(net, account.getOutstandingPrincipal());
        account.setOutstandingPrincipal(FinancialMath.money(account.getOutstandingPrincipal().subtract(principalPaid)));
        if (account.getOutstandingPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
            account.setStatus(LoanLifecycleStatus.CLOSED);
        }
    }

    private void rebuildSchedule(LoanAccountDocument account) {
        LoanRepaymentStrategy repaymentStrategy = strategyRegistry.resolveRepaymentStrategy(account.getRepaymentMethod());
        LoanScheduleContext context = buildScheduleContext(account);
        RepaymentSchedule schedule = repaymentStrategy.buildSchedule(context);
        account.setScheduleLines(schedule.lines().stream().map(this::toDocument).toList());
    }

    private LoanScheduleContext buildScheduleContext(LoanAccountDocument account) {
        return new LoanScheduleContext(
                account.getId(), account.getOutstandingPrincipal(), account.getAnnualRate(),
                account.getTenureMonths(), account.getStartDate(), account.getMaturityDate(),
                account.getInterestMethod(), account.getRateMode(), account.getRepaymentMethod(),
                account.getMonthlyFee(), account.getPenaltyRate(),
                account.getRateHistory().stream().map(this::toDomain).toList()
        );
    }

    private void accrueUntil(LoanAccountDocument account, LocalDate untilDate) {
        LocalDate start = account.getLastAccruedDate() == null ? account.getStartDate() : account.getLastAccruedDate();
        if (!untilDate.isAfter(start)) return;
        LoanInterestStrategy interest = strategyRegistry.resolveInterestStrategy(account.getInterestMethod());
        LoanRateStrategy rate = strategyRegistry.resolveRateStrategy(account.getRateMode());
        List<RateChange> history = account.getRateHistory().stream().map(this::toDomain).toList();
        BigDecimal resolvedRate = rate.resolveAnnualRate(account.getAnnualRate(), untilDate, history);
        InterestDetail detail = interest.calculate(account.getOutstandingPrincipal(), resolvedRate, start, untilDate, history);
        account.setAccruedInterest(FinancialMath.money(account.getAccruedInterest().add(detail.interestAmount())));
        account.setLastAccruedDate(untilDate);
    }

    private LoanAccountDocument load(UUID loanId) {
        return loanAccountRepo.findById(loanId)
                .orElseThrow(() -> new FinancialOperationException(HttpStatus.NOT_FOUND, "Loan account not found"));
    }

    private LoanProductDefinitionDocument product(String code) {
        return loanProductDefinitionRepo.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new FinancialOperationException(HttpStatus.NOT_FOUND, "Loan product not found: " + code));
    }

    private LoanScheduleLineDocument toDocument(RepaymentScheduleLine line) {
        LoanScheduleLineDocument doc = new LoanScheduleLineDocument();
        doc.setDueDate(line.dueDate());
        doc.setOpeningBalance(line.openingBalance());
        doc.setExpectedPrincipal(line.expectedPrincipal());
        doc.setExpectedInterest(line.expectedInterest());
        doc.setExpectedFee(line.expectedFee());
        doc.setExpectedTotal(line.expectedTotal());
        doc.setClosingBalance(line.closingBalance());
        doc.setStatus(line.status());
        return doc;
    }

    private RepaymentScheduleLine toDomain(LoanScheduleLineDocument line) {
        return new RepaymentScheduleLine(line.getDueDate(), line.getOpeningBalance(), line.getExpectedPrincipal(),
                line.getExpectedInterest(), line.getExpectedFee(), line.getExpectedTotal(),
                line.getClosingBalance(), line.getStatus());
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

    private BigDecimal min(BigDecimal left, BigDecimal right) {
        return left.min(right).setScale(FinancialMath.SCALE, RoundingMode.HALF_UP);
    }

    private void validateRange(BigDecimal value, BigDecimal min, BigDecimal max, String field) {
        if (value == null) throw new FinancialOperationException(HttpStatus.BAD_REQUEST, field + " is required");
        if (min != null && value.compareTo(min) < 0)
            throw new FinancialOperationException(HttpStatus.BAD_REQUEST, field + " is below the minimum allowed");
        if (max != null && value.compareTo(max) > 0)
            throw new FinancialOperationException(HttpStatus.BAD_REQUEST, field + " is above the maximum allowed");
    }

    private void validateTenure(Integer tenure, Integer min, Integer max) {
        if (tenure == null) return;
        if (min != null && tenure < min)
            throw new FinancialOperationException(HttpStatus.BAD_REQUEST, "tenureMonths is below the minimum allowed");
        if (max != null && tenure > max)
            throw new FinancialOperationException(HttpStatus.BAD_REQUEST, "tenureMonths is above the maximum allowed");
    }
}
