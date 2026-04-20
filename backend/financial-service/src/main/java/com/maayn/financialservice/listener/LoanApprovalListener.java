package com.maayn.financialservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanApprovalListener {

    // TODO: Inject LoanManagementService to disburse loan

    @RabbitListener(queues = "loan.approved.queue")
    public void onLoanApproved(Map<String, Object> event) {
        try {
            log.info("Received loan approval event: {}", event);

            UUID loanId = UUID.fromString(event.get("loanId").toString());

            // TODO: 
            // 1. Update loan status to APPROVED
            // 2. Create disbursement transaction
            // 3. Call Transaction Service to transfer funds to customer account
            // 4. Update loan status to DISBURSED
            // 5. Publish LoanDisbursedEvent

            log.info("Loan {} approved and ready for disbursement", loanId);

        } catch (Exception e) {
            log.error("Failed to process loan approval event", e);
        }
    }
}

