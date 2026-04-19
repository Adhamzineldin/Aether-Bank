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
import com.maayn.financialservice.repo.LoanAccountRepo;
import com.maayn.financialservice.repo.LoanProductDefinitionRepo;
import com.maayn.financialservice.strategy.loan.LoanInterestStrategy;
import com.maayn.financialservice.strategy.loan.LoanRateStrategy;
import com.maayn.financialservice.strategy.loan.LoanRepaymentStrategy;
import com.maayn.financialservice.strategy.loan.LoanScheduleContext;
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
public class LoanManagementService {

    private final LoanAccountRepo loanAccountRepo;
    private final LoanProductDefinitionRepo loanProductDefinitionRepo;
    private final LoanStrategyRegistry strategyRegistry;
    private final LedgerPort ledgerPort;

    @Transactional
    public LoanAccountDocument originate(LoanOriginationCommand request) {
        LoanProductDefinitionDocument product = product(request.productCode());
        validateRange(request.principal(), product.getMinimumPrincipal(), product.getMaximumPrincipal(), "principal");
        validateTenure(request.tenureMonths(), product.getMinimumTenureMonths(), product.getMaximumTenureMonths());

        LocalDate startDate = request.startDate() == null ? LocalDate.now() : request.startDate();
        Integer tenureMonths = request.tenureMonths() == null ? product.getDefaultTenureMonths() : request.tenureMonths();
        LocalDate maturityDate = request.maturityDate() == null ? startDate.plusMonths(tenureMonths) : request.maturityDate();
        BigDecimal annualRate = product.getBaseAnnualRate();

        LoanAccountDocument account = new LoanAccountDocument();
        account.setId(UUID.randomUUID());
        account.setCustomerId(request.customerId());
        account.setLoanNumber("LN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        account.setApplicationId(request.applicationId());
        account.setProductCode(product.getCode());
        account.setLoanType(product.getLoanType());
        account.setPrincipal(FinancialMath.money(request.principal()));
        account.setOutstandingPrincipal(FinancialMath.money(request.principal()));
        account.setAccruedInterest(BigDecimal.ZERO.setScale(FinancialMath.SCALE, RoundingMode.HALF_UP));
        account.setAnnualRate(annualRate);
        account.setInterestMethod(product.getInterestMethod());
        account.setRateMode(product.getRateMode());
        account.setRepaymentMethod(product.getRepaymentMethod());
        account.setMonthlyFee(FinancialMath.money(product.getMonthlyFee()));
        account.setPenaltyRate(FinancialMath.money(product.getPenaltyRate()));
        account.setTenureMonths(tenureMonths);
        account.setStartDate(startDate);
        account.setMaturityDate(maturityDate);
        account.setNextDueDate(startDate.plusMonths(1));
        account.setStatus(LoanLifecycleStatus.ACTIVE);
        account.setScheduleLines(new ArrayList<>());
        account.setRateHistory(new ArrayList<>());
        account.setLastAccruedDate(startDate);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        rebuildSchedule(account);
        loanAccountRepo.save(account);
        ledgerPort.recordLoanEntry(account.getId(), "LOAN_ORIGINATED", account.getPrincipal(), "LoanReceivable", "CustomerCash", "Loan originated from product " + product.getCode());
        return account;
    }

    @Transactional
    public LoanAccountDocument accrueInterest(UUID loanId, LocalDate accrualDate) {
        LoanAccountDocument account = load(loanId);
        accrueUntil(account, accrualDate == null ? LocalDate.now() : accrualDate);
        loanAccountRepo.save(account);
        return account;
    }

    @Transactional
    public LoanAccountDocument processPayment(UUID loanId, Payment payment) {
        LoanAccountDocument account = load(loanId);
        LocalDate paymentDate = payment.paymentDate() == null ? LocalDate.now() : payment.paymentDate().toLocalDate();
        accrueUntil(account, paymentDate);

        BigDecimal amount = FinancialMath.money(payment.amount());
        BigDecimal penalty = BigDecimal.ZERO.setScale(FinancialMath.SCALE, RoundingMode.HALF_UP);
        if (account.getNextDueDate() != null && paymentDate.isAfter(account.getNextDueDate())) {
            penalty = FinancialMath.money(amount.multiply(account.getPenaltyRate()));
            account.setAccruedInterest(FinancialMath.money(account.getAccruedInterest().add(penalty)));
        }

        BigDecimal remaining = amount;
        BigDecimal interestPaid = min(remaining, account.getAccruedInterest());
        remaining = remaining.subtract(interestPaid);
        account.setAccruedInterest(FinancialMath.money(account.getAccruedInterest().subtract(interestPaid)));

        BigDecimal principalPaid = min(remaining, account.getOutstandingPrincipal());
        remaining = remaining.subtract(principalPaid);
        account.setOutstandingPrincipal(FinancialMath.money(account.getOutstandingPrincipal().subtract(principalPaid)));

        if (account.getOutstandingPrincipal().compareTo(BigDecimal.ZERO) <= 0 && account.getAccruedInterest().compareTo(BigDecimal.ZERO) <= 0) {
            account.setStatus(LoanLifecycleStatus.CLOSED);
        }
        account.setNextDueDate(paymentDate.plusMonths(1));
        account.setUpdatedAt(LocalDateTime.now());
        rebuildSchedule(account);
        loanAccountRepo.save(account);

        ledgerPort.recordLoanEntry(account.getId(), "LOAN_PAYMENT", amount, "CustomerCash", "LoanReceivable", "Loan payment posted" + (payment.reference() == null ? "" : " ref=" + payment.reference()));
        return account;
    }

    @Transactional
    public LoanAccountDocument earlyRepayment(UUID loanId, BigDecimal amount, LocalDate repaymentDate) {
        LoanAccountDocument account = load(loanId);
        LocalDate date = repaymentDate == null ? LocalDate.now() : repaymentDate;
        accrueUntil(account, date);
        BigDecimal requested = FinancialMath.money(amount);
        BigDecimal penalty = FinancialMath.money(requested.multiply(account.getPenaltyRate()));
        BigDecimal net = requested.subtract(penalty);
        BigDecimal principalPaid = min(net, account.getOutstandingPrincipal());
        account.setOutstandingPrincipal(FinancialMath.money(account.getOutstandingPrincipal().subtract(principalPaid)));
        if (account.getOutstandingPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
            account.setStatus(LoanLifecycleStatus.CLOSED);
        }
        rebuildSchedule(account);
        account.setUpdatedAt(LocalDateTime.now());
        loanAccountRepo.save(account);
        ledgerPort.recordLoanEntry(account.getId(), "LOAN_EARLY_REPAYMENT", requested, "CustomerCash", "LoanReceivable", "Early repayment with penalty");
        return account;
    }

    @Transactional
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
        ledgerPort.recordLoanEntry(account.getId(), "LOAN_LATE_PENALTY", penalty, "PenaltyReceivable", "PenaltyIncome", "Late payment penalty assessed");
        return account;
    }

    @Transactional
    public LoanAccountDocument updateRate(UUID loanId, RateChange change) {
        LoanAccountDocument account = load(loanId);
        RateChangeDocument document = new RateChangeDocument();
        document.setEffectiveDate(change.effectiveDate());
        document.setAnnualRate(change.annualRate());
        document.setSource(change.source());
        document.setNotes(change.notes());
        account.getRateHistory().add(document);
        account.setAnnualRate(change.annualRate());
        rebuildSchedule(account);
        account.setUpdatedAt(LocalDateTime.now());
        loanAccountRepo.save(account);
        return account;
    }

    @Transactional(readOnly = true)
    public RepaymentSchedule schedule(UUID loanId) {
        LoanAccountDocument account = load(loanId);
        return new RepaymentSchedule(account.getId(), account.getScheduleLines().stream().map(this::toDomain).toList());
    }

    @Transactional(readOnly = true)
    public LoanAccountDocument get(UUID loanId) {
        return load(loanId);
    }

    private void rebuildSchedule(LoanAccountDocument account) {
        LoanRepaymentStrategy repaymentStrategy = strategyRegistry.resolveRepaymentStrategy(account.getRepaymentMethod());
        LoanScheduleContext context = new LoanScheduleContext(
                account.getId(),
                account.getOutstandingPrincipal(),
                account.getAnnualRate(),
                account.getTenureMonths(),
                account.getStartDate(),
                account.getMaturityDate(),
                account.getInterestMethod(),
                account.getRateMode(),
                account.getRepaymentMethod(),
                account.getMonthlyFee(),
                account.getPenaltyRate(),
                account.getRateHistory().stream().map(this::toDomain).toList()
        );
        RepaymentSchedule schedule = repaymentStrategy.buildSchedule(context);
        account.setScheduleLines(schedule.lines().stream().map(this::toDocument).toList());
    }

    private void accrueUntil(LoanAccountDocument account, LocalDate untilDate) {
        LocalDate start = account.getLastAccruedDate() == null ? account.getStartDate() : account.getLastAccruedDate();
        if (!untilDate.isAfter(start)) {
            return;
        }
        LoanInterestStrategy interestStrategy = strategyRegistry.resolveInterestStrategy(account.getInterestMethod());
        LoanRateStrategy rateStrategy = strategyRegistry.resolveRateStrategy(account.getRateMode());
        BigDecimal resolvedRate = rateStrategy.resolveAnnualRate(account.getAnnualRate(), untilDate, account.getRateHistory().stream().map(this::toDomain).toList());
        InterestDetail detail = interestStrategy.calculate(account.getOutstandingPrincipal(), resolvedRate, start, untilDate, account.getRateHistory().stream().map(this::toDomain).toList());
        account.setAccruedInterest(FinancialMath.money(account.getAccruedInterest().add(detail.interestAmount())));
        account.setLastAccruedDate(untilDate);
    }

    private LoanAccountDocument load(UUID loanId) {
        return loanAccountRepo.findById(loanId).orElseThrow(() -> new FinancialOperationException(HttpStatus.NOT_FOUND, "Loan account not found"));
    }

    private LoanProductDefinitionDocument product(String code) {
        return loanProductDefinitionRepo.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new FinancialOperationException(HttpStatus.NOT_FOUND, "Loan product not found: " + code));
    }

    private LoanScheduleLineDocument toDocument(RepaymentScheduleLine line) {
        LoanScheduleLineDocument document = new LoanScheduleLineDocument();
        document.setDueDate(line.dueDate());
        document.setOpeningBalance(line.openingBalance());
        document.setExpectedPrincipal(line.expectedPrincipal());
        document.setExpectedInterest(line.expectedInterest());
        document.setExpectedFee(line.expectedFee());
        document.setExpectedTotal(line.expectedTotal());
        document.setClosingBalance(line.closingBalance());
        document.setStatus(line.status());
        return document;
    }

    private RepaymentScheduleLine toDomain(LoanScheduleLineDocument line) {
        return new RepaymentScheduleLine(
                line.getDueDate(),
                line.getOpeningBalance(),
                line.getExpectedPrincipal(),
                line.getExpectedInterest(),
                line.getExpectedFee(),
                line.getExpectedTotal(),
                line.getClosingBalance(),
                line.getStatus()
        );
    }

    private RateChange toDomain(RateChangeDocument change) {
        return new RateChange(change.getEffectiveDate(), change.getAnnualRate(), change.getSource(), change.getNotes());
    }

    private BigDecimal min(BigDecimal left, BigDecimal right) {
        return left.min(right).setScale(FinancialMath.SCALE, RoundingMode.HALF_UP);
    }

    private void validateRange(BigDecimal value, BigDecimal min, BigDecimal max, String field) {
        if (value == null) {
            throw new FinancialOperationException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        if (min != null && value.compareTo(min) < 0) {
            throw new FinancialOperationException(HttpStatus.BAD_REQUEST, field + " is below the minimum allowed");
        }
        if (max != null && value.compareTo(max) > 0) {
            throw new FinancialOperationException(HttpStatus.BAD_REQUEST, field + " is above the maximum allowed");
        }
    }

    private void validateTenure(Integer tenureMonths, Integer min, Integer max) {
        if (tenureMonths == null) {
            return;
        }
        if (min != null && tenureMonths < min) {
            throw new FinancialOperationException(HttpStatus.BAD_REQUEST, "tenureMonths is below the minimum allowed");
        }
        if (max != null && tenureMonths > max) {
            throw new FinancialOperationException(HttpStatus.BAD_REQUEST, "tenureMonths is above the maximum allowed");
        }
    }
}

record LoanOriginationCommand(
        UUID customerId,
        UUID applicationId,
        String productCode,
        BigDecimal principal,
        Integer tenureMonths,
        LocalDate startDate,
        LocalDate maturityDate
) {
}
