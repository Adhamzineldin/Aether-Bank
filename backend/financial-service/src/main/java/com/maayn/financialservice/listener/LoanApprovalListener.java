package com.maayn.financialservice.listener;

import com.maayn.financialservice.entity.LoanApplicationDocument;
import com.maayn.financialservice.events.FinancialEventPublisher;
import com.maayn.financialservice.gateway.TransactionGateway;
import com.maayn.financialservice.repo.LoanRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.certificate.ApplicationStatus;
import maayn.veld.generated.models.shared.LoanStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanApprovalListener {

    private final LoanRepo loanRepository;
    private final TransactionGateway transactionGateway;
    private final FinancialEventPublisher eventPublisher;

    @RabbitListener(queues = "loan.approved.queue")
    public void onLoanApproved(Map<String, Object> event) {
        try {
            log.info("Received loan approval event: {}", event);

            UUID loanId = UUID.fromString(event.get("loanId").toString());

            // 1. Update loan status to APPROVED
            LoanApplicationDocument loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));

            if (loan.getAccountId() == null) {
                throw new IllegalStateException("Loan " + loanId + " has no disbursement accountId; cannot disburse");
            }
            if (loan.getPrincipalAmount() == null || loan.getPrincipalAmount().signum() <= 0) {
                throw new IllegalStateException("Loan " + loanId + " has no principal amount");
            }
            if (loan.getTenureMonths() == null || loan.getTenureMonths() <= 0) {
                throw new IllegalStateException("Loan " + loanId + " has no tenure months");
            }

            loan.setApplicationStatus(ApplicationStatus.APPROVED);
            loan.setLoanStatus(LoanStatus.APPROVED);
            loan.setReviewedAt(LocalDateTime.now());
            loan.setUpdatedAt(LocalDateTime.now());
            loanRepository.save(loan);

            log.info("Loan {} marked as APPROVED", loanId);

            // 2. Disburse loan funds via Transaction Service (uses shared gateway,
            //    same path as MortgageApprovalListener / CertificateApprovalListener).
            transactionGateway.disburseLoan(
                    loan.getAccountId(),
                    loan.getPrincipalAmount(),
                    loan.getCurrency() != null ? loan.getCurrency() : "USD",
                    "loan-disburse-" + loan.getId());

            log.info("Loan funds transferred: {} {} to account {}",
                    loan.getPrincipalAmount(), loan.getCurrency(), loan.getAccountId());

            // 3. Update loan status to DISBURSED
            loan.setLoanStatus(LoanStatus.DISBURSED);
            loan.setDisbursedAmount(loan.getPrincipalAmount());
            loan.setDisbursementDate(LocalDateTime.now());
            loan.setStartDate(LocalDateTime.now().toLocalDate());
            loan.setEndDate(loan.getStartDate().plusMonths(loan.getTenureMonths()));
            loan.setOutstandingBalance(loan.getPrincipalAmount());
            loan.setUpdatedAt(LocalDateTime.now());
            loanRepository.save(loan);

            log.info("Loan {} successfully disbursed", loanId);

            // 4. Publish LoanDisbursedEvent
            eventPublisher.publishLoanDisbursed(
                loan.getId(),
                loan.getCustomerId(),
                loan.getDisbursedAmount()
            );

        } catch (Exception e) {
            log.error("Failed to process loan approval event: {}", event, e);
            throw new RuntimeException("Loan approval handling failed", e);
        }
    }
}
