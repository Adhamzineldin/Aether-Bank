package com.maayn.financialservice.listener;

import com.maayn.financialservice.entity.MortgageApplicationDocument;
import com.maayn.financialservice.gateway.TransactionGateway;
import com.maayn.financialservice.repo.MortgageRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.certificate.ApplicationStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MortgageApprovalListener {

    private final MortgageRepo mortgageRepository;
    private final TransactionGateway transactionGateway;

    @RabbitListener(queues = "mortgage.approved.queue")
    public void onMortgageApproved(Map<String, Object> event) {
        try {
            log.info("Received mortgage approval event: {}", event);

            UUID mortgageId = UUID.fromString(event.get("mortgageId").toString());
            MortgageApplicationDocument mortgage = mortgageRepository.findById(mortgageId)
                    .orElseThrow(() -> new RuntimeException("Mortgage not found: " + mortgageId));

            if (mortgage.getAccountId() == null) {
                throw new IllegalStateException("Mortgage " + mortgageId + " has no settlement accountId; cannot disburse");
            }
            if (mortgage.getPrincipalAmount() == null || mortgage.getPrincipalAmount().signum() <= 0) {
                throw new IllegalStateException("Mortgage " + mortgageId + " has no principal amount");
            }

            mortgage.setApplicationStatus(ApplicationStatus.APPROVED);
            mortgage.setReviewedAt(LocalDateTime.now());
            mortgage.setUpdatedAt(LocalDateTime.now());
            mortgageRepository.save(mortgage);

            transactionGateway.disburseLoan(
                    mortgage.getAccountId(),
                    mortgage.getPrincipalAmount(),
                    mortgage.getCurrency() != null ? mortgage.getCurrency() : "USD",
                    "mortgage-disburse-" + mortgage.getId());

            mortgage.setMortgageStatus("ACTIVE");
            mortgage.setDisbursedAmount(mortgage.getPrincipalAmount());
            mortgage.setDisbursementDate(LocalDateTime.now());
            mortgage.setStartDate(LocalDate.now());
            if (mortgage.getTermMonths() > 0) {
                mortgage.setEndDate(mortgage.getStartDate().plusMonths(mortgage.getTermMonths()));
            }
            mortgage.setOutstandingBalance(mortgage.getPrincipalAmount());
            mortgage.setUpdatedAt(LocalDateTime.now());
            mortgageRepository.save(mortgage);

            log.info("Mortgage {} disbursed to account {}", mortgageId, mortgage.getAccountId());
        } catch (Exception e) {
            log.error("Failed to process mortgage approval event", e);
        }
    }
}
