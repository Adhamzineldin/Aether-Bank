package com.maayn.notificationservice.events;

import com.maayn.notificationservice.entity.WorkflowInstance;
import com.maayn.notificationservice.entity.WorkflowStep;
import com.maayn.notificationservice.entity.WorkflowTemplate;
import com.maayn.notificationservice.repository.WorkflowInstanceRepository;
import com.maayn.notificationservice.repository.WorkflowTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationListener {

    private final WorkflowTemplateRepository templateRepository;
    private final WorkflowInstanceRepository instanceRepository;

    @RabbitListener(queues = "loan.submitted.queue")
    public void onLoanSubmitted(Map<String, Object> event) {
        try {
            log.info("Received loan submission event: {}", event);

            UUID loanId = UUID.fromString(event.get("loanId").toString());
            UUID customerId = UUID.fromString(event.get("customerId").toString());

            // Find LOAN workflow template
            WorkflowTemplate template = templateRepository.findByEntityType("LOAN")
                    .orElseThrow(() -> new RuntimeException("LOAN workflow template not found"));

            // Create workflow instance
            WorkflowInstance instance = WorkflowInstance.builder()
                    .id(UUID.randomUUID())
                    .templateId(template.getId())
                    .entityType("LOAN")
                    .entityId(loanId)
                    .status("PENDING")
                    .currentStep(1)
                    .steps(template.getSteps())
                    .version(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            instanceRepository.save(instance);
            log.info("Created workflow instance for loan: {}", loanId);

            // TODO: Create first approval task and notify employee

        } catch (Exception e) {
            log.error("Failed to process loan submission event", e);
        }
    }
}

