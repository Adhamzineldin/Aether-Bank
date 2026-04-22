package com.maayn.financialservice.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinancialEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "bank.events";

    public void publishLoanSubmitted(UUID loanId, UUID customerId, java.math.BigDecimal amount) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("loanId", loanId.toString());
            event.put("customerId", customerId.toString());
            event.put("amount", amount.toString());
            event.put("timestamp", java.time.LocalDateTime.now().toString());

            rabbitTemplate.convertAndSend(EXCHANGE, "loan.submitted", event);
            log.info("Published LoanSubmittedEvent for loan: {}", loanId);
        } catch (Exception e) {
            log.error("Failed to publish LoanSubmittedEvent", e);
        }
    }

    public void publishMortgageSubmitted(UUID mortgageId, UUID customerId, java.math.BigDecimal amount) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("mortgageId", mortgageId.toString());
            event.put("customerId", customerId.toString());
            event.put("amount", amount.toString());
            event.put("timestamp", java.time.LocalDateTime.now().toString());

            rabbitTemplate.convertAndSend(EXCHANGE, "mortgage.submitted", event);
            log.info("Published MortgageSubmittedEvent for mortgage: {}", mortgageId);
        } catch (Exception e) {
            log.error("Failed to publish MortgageSubmittedEvent", e);
        }
    }

    public void publishCertificateSubmitted(UUID certificateId, UUID customerId, java.math.BigDecimal amount) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("certificateId", certificateId.toString());
            event.put("customerId", customerId.toString());
            event.put("amount", amount.toString());
            event.put("timestamp", java.time.LocalDateTime.now().toString());

            rabbitTemplate.convertAndSend(EXCHANGE, "certificate.submitted", event);
            log.info("Published CertificateSubmittedEvent for certificate: {}", certificateId);
        } catch (Exception e) {
            log.error("Failed to publish CertificateSubmittedEvent", e);
        }
    }

    public void publishLoanDisbursed(UUID loanId, UUID customerId, java.math.BigDecimal amount) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("loanId", loanId.toString());
            event.put("customerId", customerId.toString());
            event.put("amount", amount.toString());
            event.put("timestamp", java.time.LocalDateTime.now().toString());

            rabbitTemplate.convertAndSend(EXCHANGE, "loan.disbursed", event);
            log.info("Published LoanDisbursedEvent for loan: {}", loanId);
        } catch (Exception e) {
            log.error("Failed to publish LoanDisbursedEvent", e);
        }
    }
}

