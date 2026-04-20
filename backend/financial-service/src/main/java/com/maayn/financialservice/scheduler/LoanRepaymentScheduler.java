package com.maayn.financialservice.scheduler;

import com.maayn.financialservice.entity.LoanApplicationDocument;
import com.maayn.financialservice.events.FinancialEventPublisher;
import com.maayn.financialservice.repo.LoanRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.shared.LoanStatus;
import maayn.veld.generated.sdk.transaction.TransactionClient;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import maayn.veld.generated.sdk.transaction.models.transaction.TransferRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanRepaymentScheduler {

    private final LoanRepo loanRepository;
    private final TransactionClient transactionClient;
    private final FinancialEventPublisher eventPublisher;

    // Bank's loan repayment collection account
    private static final UUID LOAN_REPAYMENT_ACCOUNT = UUID.fromString("22222222-2222-2222-2222-222222222222");

    /**
     * Runs every day at 2 AM to process loan EMI deductions
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void processLoanRepayments() {
        log.info("Starting daily loan repayment processing...");

        LocalDate today = LocalDate.now();
        
        // Find all active loans
        List<LoanApplicationDocument> activeLoans = loanRepository.findByLoanStatus(LoanStatus.ACTIVE);
        
        log.info("Found {} active loans to process", activeLoans.size());

        for (LoanApplicationDocument loan : activeLoans) {
            try {
                // Check if EMI is due today (assume monthly repayment on same day as start date)
                if (isEmiDueToday(loan, today)) {
                    processEmiDeduction(loan);
                }
            } catch (Exception e) {
                log.error("Failed to process EMI for loan: {}", loan.getId(), e);
                // Mark loan as OVERDUE if payment fails
                updateLoanStatus(loan, LoanStatus.OVERDUE);
            }
        }

        log.info("Loan repayment processing completed");
    }

    /**
     * Runs every month on 1st day at 3 AM to calculate and add interest
     */
    @Scheduled(cron = "0 0 3 1 * *")
    public void calculateMonthlyInterest() {
        log.info("Starting monthly interest calculation...");

        List<LoanApplicationDocument> activeLoans = loanRepository.findByLoanStatus(LoanStatus.ACTIVE);

        for (LoanApplicationDocument loan : activeLoans) {
            try {
                calculateAndAddInterest(loan);
            } catch (Exception e) {
                log.error("Failed to calculate interest for loan: {}", loan.getId(), e);
            }
        }

        log.info("Monthly interest calculation completed");
    }

    private boolean isEmiDueToday(LoanApplicationDocument loan, LocalDate today) {
        if (loan.getStartDate() == null) {
            return false;
        }

        // EMI due on same day each month as start date
        int startDay = loan.getStartDate().getDayOfMonth();
        int todayDay = today.getDayOfMonth();

        return startDay == todayDay;
    }

    private void processEmiDeduction(LoanApplicationDocument loan) {
        log.info("Processing EMI deduction for loan: {}", loan.getLoanNumber());

        BigDecimal emiAmount = loan.getEmiAmount();
        if (emiAmount == null || emiAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid EMI amount for loan: {}", loan.getId());
            return;
        }

        // Deduct EMI from customer's account
        try {
            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setIdempotencyKey("emi-" + loan.getId() + "-" + LocalDate.now());
            transferRequest.setSourceAccountId(loan.getAccountId());
            transferRequest.setDestinationAccountId(LOAN_REPAYMENT_ACCOUNT);
            transferRequest.setAmount(emiAmount);
            transferRequest.setCurrency(loan.getCurrency());
            transferRequest.setSourceCurrency(loan.getCurrency());
            transferRequest.setDestinationCurrency(loan.getCurrency());
            transferRequest.setType(TransactionType.INTERNAL_TRANSFER);

            transactionClient.transaction.transfer(transferRequest);

            // Update outstanding balance
            BigDecimal newBalance = loan.getOutstandingBalance().subtract(calculatePrincipalComponent(loan, emiAmount));
            loan.setOutstandingBalance(newBalance);

            // Check if loan is fully repaid
            if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
                updateLoanStatus(loan, LoanStatus.CLOSED);
                log.info("Loan {} fully repaid and closed", loan.getLoanNumber());
            }

            loanRepository.save(loan);
            log.info("EMI deducted successfully for loan: {}", loan.getLoanNumber());

        } catch (Exception e) {
            log.error("Failed to deduct EMI for loan: {}", loan.getId(), e);
            updateLoanStatus(loan, LoanStatus.OVERDUE);
            throw new RuntimeException("EMI deduction failed", e);
        }
    }

    private void calculateAndAddInterest(LoanApplicationDocument loan) {
        log.info("Calculating monthly interest for loan: {}", loan.getLoanNumber());

        BigDecimal monthlyInterestRate = loan.getInterestRate()
                .divide(new BigDecimal("100"), 6, BigDecimal.ROUND_HALF_UP)
                .divide(new BigDecimal("12"), 6, BigDecimal.ROUND_HALF_UP);

        BigDecimal interestAmount = loan.getOutstandingBalance()
                .multiply(monthlyInterestRate)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        // Add interest to outstanding balance
        loan.setOutstandingBalance(loan.getOutstandingBalance().add(interestAmount));
        loanRepository.save(loan);

        log.info("Interest {} added to loan: {}", interestAmount, loan.getLoanNumber());
    }

    private BigDecimal calculatePrincipalComponent(LoanApplicationDocument loan, BigDecimal emiAmount) {
        // Calculate interest portion
        BigDecimal monthlyInterestRate = loan.getInterestRate()
                .divide(new BigDecimal("100"), 6, BigDecimal.ROUND_HALF_UP)
                .divide(new BigDecimal("12"), 6, BigDecimal.ROUND_HALF_UP);

        BigDecimal interestPortion = loan.getOutstandingBalance()
                .multiply(monthlyInterestRate)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        // Principal = EMI - Interest
        return emiAmount.subtract(interestPortion);
    }

    private void updateLoanStatus(LoanApplicationDocument loan, LoanStatus status) {
        loan.setLoanStatus(status);
        loanRepository.save(loan);
        log.info("Loan {} status updated to: {}", loan.getLoanNumber(), status);
    }
}

