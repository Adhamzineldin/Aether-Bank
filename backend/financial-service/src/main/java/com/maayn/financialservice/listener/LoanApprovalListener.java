package com.maayn.financialservice.listener;

import com.maayn.financialservice.entity.LoanApplicationDocument;
import com.maayn.financialservice.events.FinancialEventPublisher;
import com.maayn.financialservice.repo.LoanRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.certificate.ApplicationStatus;
import maayn.veld.generated.models.shared.LoanStatus;
import maayn.veld.generated.sdk.transaction.TransactionClient;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import maayn.veld.generated.sdk.transaction.models.transaction.TransferRequest;
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
    private final TransactionClient transactionClient;
    private final FinancialEventPublisher eventPublisher;

    // Bank's loan disbursement account
    private static final UUID LOAN_VAULT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @RabbitListener(queues = "loan.approved.queue")
    public void onLoanApproved(Map<String, Object> event) {
        try {
            log.info("Received loan approval event: {}", event);

            UUID loanId = UUID.fromString(event.get("loanId").toString());
            
            // 1. Update loan status to APPROVED
            LoanApplicationDocument loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));

            loan.setApplicationStatus(ApplicationStatus.APPROVED);
            loan.setLoanStatus(LoanStatus.APPROVED);
            loan.setReviewedAt(LocalDateTime.now());
            loan.setUpdatedAt(LocalDateTime.now());
            loanRepository.save(loan);

            log.info("Loan {} marked as APPROVED", loanId);

            // 2. Disburse loan funds via Transaction Service
            disburseLoan(loan);

            // 3. Update loan status to DISBURSED
            loan.setLoanStatus(LoanStatus.DISBURSED);
            loan.setDisbursedAmount(loan.getPrincipalAmount());
            loan.setDisbursementDate(LocalDateTime.now());
            loan.setStartDate(LocalDateTime.now().toLocalDate());
            loan.setEndDate(loan.getStartDate().plusMonths(loan.getTenureMonths()));
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
            log.error("Failed to process loan approval event", e);
            // TODO: Implement dead-letter queue for failed disbursements
        }
    }

    private void disburseLoan(LoanApplicationDocument loan) {
        try {
            // Transfer funds from LOAN_VAULT to customer's account
            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setIdempotencyKey("loan-disburse-" + loan.getId());
            transferRequest.setSourceAccountId(LOAN_VAULT_ID);
            transferRequest.setDestinationAccountId(loan.getAccountId());
            transferRequest.setAmount(loan.getPrincipalAmount());
            transferRequest.setCurrency(loan.getCurrency());
            transferRequest.setSourceCurrency(loan.getCurrency());
            transferRequest.setDestinationCurrency(loan.getCurrency());
            transferRequest.setType(TransactionType.INTERNAL_TRANSFER);

            transactionClient.transaction.transfer(transferRequest);
            
            log.info("Loan funds transferred: {} {} to account {}", 
                    loan.getPrincipalAmount(), loan.getCurrency(), loan.getAccountId());

        } catch (Exception e) {
            log.error("Failed to disburse loan funds for loan: {}", loan.getId(), e);
            throw new RuntimeException("Loan disbursement failed", e);
        }
    }
}

