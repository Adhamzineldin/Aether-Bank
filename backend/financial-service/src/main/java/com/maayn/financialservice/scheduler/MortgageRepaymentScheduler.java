package com.maayn.financialservice.scheduler;

import com.maayn.financialservice.entity.MortgageApplicationDocument;
import com.maayn.financialservice.repo.MortgageRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class MortgageRepaymentScheduler {

    private final MortgageRepo mortgageRepository;
    private final TransactionClient transactionClient;

    // Bank's mortgage repayment collection account
    private static final UUID MORTGAGE_REPAYMENT_ACCOUNT = UUID.fromString("44444444-4444-4444-4444-444444444444");

    /**
     * Runs every day at 2:30 AM to process mortgage monthly payments
     */
    @Scheduled(cron = "0 30 2 * * *")
    public void processMortgagePayments() {
        log.info("Starting daily mortgage payment processing...");

        LocalDate today = LocalDate.now();
        
        // Find all active mortgages
        List<MortgageApplicationDocument> activeMortgages = 
            mortgageRepository.findByMortgageStatus("ACTIVE");
        
        log.info("Found {} active mortgages to process", activeMortgages.size());

        for (MortgageApplicationDocument mortgage : activeMortgages) {
            try {
                if (isPaymentDueToday(mortgage, today)) {
                    processMonthlyPayment(mortgage);
                }
            } catch (Exception e) {
                log.error("Failed to process payment for mortgage: {}", mortgage.getId(), e);
                updateMortgageStatus(mortgage, "OVERDUE");
            }
        }

        log.info("Mortgage payment processing completed");
    }

    /**
     * Runs every month on 1st day at 3:30 AM to calculate and add interest
     */
    @Scheduled(cron = "0 30 3 1 * *")
    public void calculateMonthlyInterest() {
        log.info("Starting monthly interest calculation for mortgages...");

        List<MortgageApplicationDocument> activeMortgages = 
            mortgageRepository.findByMortgageStatus("ACTIVE");

        for (MortgageApplicationDocument mortgage : activeMortgages) {
            try {
                calculateAndAddInterest(mortgage);
            } catch (Exception e) {
                log.error("Failed to calculate interest for mortgage: {}", mortgage.getId(), e);
            }
        }

        log.info("Monthly interest calculation completed");
    }

    private boolean isPaymentDueToday(MortgageApplicationDocument mortgage, LocalDate today) {
        if (mortgage.getStartDate() == null) {
            return false;
        }

        // Payment due on same day each month as start date
        int startDay = mortgage.getStartDate().getDayOfMonth();
        int todayDay = today.getDayOfMonth();

        return startDay == todayDay;
    }

    private void processMonthlyPayment(MortgageApplicationDocument mortgage) {
        log.info("Processing monthly payment for mortgage: {}", mortgage.getMortgageNumber());

        BigDecimal monthlyPayment = mortgage.getMonthlyPayment();
        if (monthlyPayment == null || monthlyPayment.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid monthly payment for mortgage: {}", mortgage.getId());
            return;
        }

        // Deduct monthly payment from customer's account
        try {
            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setIdempotencyKey("mortgage-" + mortgage.getId() + "-" + LocalDate.now());
            transferRequest.setSourceAccountId(mortgage.getAccountId());
            transferRequest.setDestinationAccountId(MORTGAGE_REPAYMENT_ACCOUNT);
            transferRequest.setAmount(monthlyPayment);
            transferRequest.setCurrency(mortgage.getCurrency());
            transferRequest.setSourceCurrency(mortgage.getCurrency());
            transferRequest.setDestinationCurrency(mortgage.getCurrency());
            transferRequest.setType(TransactionType.INTERNAL_TRANSFER);

            transactionClient.transaction.transfer(transferRequest);

            // Update outstanding balance
            BigDecimal principalPortion = calculatePrincipalComponent(mortgage, monthlyPayment);
            BigDecimal newBalance = mortgage.getOutstandingBalance().subtract(principalPortion);
            mortgage.setOutstandingBalance(newBalance);

            // Check if mortgage is fully paid
            if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
                updateMortgageStatus(mortgage, "CLOSED");
                log.info("Mortgage {} fully paid and closed", mortgage.getMortgageNumber());
            }

            mortgageRepository.save(mortgage);
            log.info("Monthly payment deducted successfully for mortgage: {}", mortgage.getMortgageNumber());

        } catch (Exception e) {
            log.error("Failed to deduct payment for mortgage: {}", mortgage.getId(), e);
            updateMortgageStatus(mortgage, "OVERDUE");
            throw new RuntimeException("Mortgage payment deduction failed", e);
        }
    }

    private void calculateAndAddInterest(MortgageApplicationDocument mortgage) {
        log.info("Calculating monthly interest for mortgage: {}", mortgage.getMortgageNumber());

        BigDecimal monthlyInterestRate = mortgage.getInterestRate()
                .divide(new BigDecimal("100"), 6, BigDecimal.ROUND_HALF_UP)
                .divide(new BigDecimal("12"), 6, BigDecimal.ROUND_HALF_UP);

        BigDecimal interestAmount = mortgage.getOutstandingBalance()
                .multiply(monthlyInterestRate)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        // Add interest to outstanding balance
        mortgage.setOutstandingBalance(mortgage.getOutstandingBalance().add(interestAmount));
        mortgageRepository.save(mortgage);

        log.info("Interest {} added to mortgage: {}", interestAmount, mortgage.getMortgageNumber());
    }

    private BigDecimal calculatePrincipalComponent(MortgageApplicationDocument mortgage, BigDecimal monthlyPayment) {
        // Calculate interest portion
        BigDecimal monthlyInterestRate = mortgage.getInterestRate()
                .divide(new BigDecimal("100"), 6, BigDecimal.ROUND_HALF_UP)
                .divide(new BigDecimal("12"), 6, BigDecimal.ROUND_HALF_UP);

        BigDecimal interestPortion = mortgage.getOutstandingBalance()
                .multiply(monthlyInterestRate)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        // Principal = Monthly Payment - Interest
        return monthlyPayment.subtract(interestPortion);
    }

    private void updateMortgageStatus(MortgageApplicationDocument mortgage, String status) {
        mortgage.setMortgageStatus(status);
        mortgageRepository.save(mortgage);
        log.info("Mortgage {} status updated to: {}", mortgage.getMortgageNumber(), status);
    }
}

