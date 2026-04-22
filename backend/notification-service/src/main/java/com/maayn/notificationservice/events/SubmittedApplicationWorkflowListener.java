package com.maayn.notificationservice.events;

import com.maayn.notificationservice.entity.ApprovalTask;
import com.maayn.notificationservice.entity.WorkflowInstance;
import com.maayn.notificationservice.entity.WorkflowStep;
import com.maayn.notificationservice.entity.WorkflowTemplate;
import com.maayn.notificationservice.repository.ApprovalTaskRepository;
import com.maayn.notificationservice.repository.WorkflowInstanceRepository;
import com.maayn.notificationservice.repository.WorkflowTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Starts a {@link WorkflowInstance} when financial-service publishes
 * {@code loan.submitted}, {@code certificate.submitted}, or {@code mortgage.submitted}
 * on {@code bank.events}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubmittedApplicationWorkflowListener {

    private final WorkflowTemplateRepository templateRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final ApprovalTaskRepository taskRepository;

    @RabbitListener(queues = "loan.submitted.queue")
    public void onLoanSubmitted(Map<String, Object> event) {
        UUID loanId = UUID.fromString(event.get("loanId").toString());
        bootstrapWorkflow("LOAN", loanId, event);
    }

    @RabbitListener(queues = "certificate.submitted.queue")
    public void onCertificateSubmitted(Map<String, Object> event) {
        UUID certificateId = UUID.fromString(event.get("certificateId").toString());
        bootstrapWorkflow("CERTIFICATE", certificateId, event);
    }

    @RabbitListener(queues = "mortgage.submitted.queue")
    public void onMortgageSubmitted(Map<String, Object> event) {
        UUID mortgageId = UUID.fromString(event.get("mortgageId").toString());
        bootstrapWorkflow("MORTGAGE", mortgageId, event);
    }

    private void bootstrapWorkflow(String entityType, UUID entityId, Map<String, Object> event) {
        try {
            log.info("Received {} submission event: {}", entityType, event);

            WorkflowTemplate template = templateRepository.findByEntityType(entityType)
                    .orElseThrow(() -> new RuntimeException(entityType + " workflow template not found"));

            WorkflowInstance instance = WorkflowInstance.builder()
                    .id(UUID.randomUUID())
                    .templateId(template.getId())
                    .entityType(entityType)
                    .entityId(entityId)
                    .status("PENDING")
                    .currentStep(1)
                    .steps(template.getSteps())
                    .version(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            instanceRepository.save(instance);
            log.info("Created workflow instance {} for {} {}", instance.getId(), entityType, entityId);

            List<WorkflowStep> steps = template.getSteps();
            if (steps != null && !steps.isEmpty()) {
                WorkflowStep first = steps.stream()
                        .filter(s -> s.getStep() == 1)
                        .findFirst()
                        .orElse(steps.get(0));

                ApprovalTask firstTask = ApprovalTask.builder()
                        .id(UUID.randomUUID())
                        .workflowId(instance.getId())
                        .step(first.getStep())
                        .role(first.getRole())
                        .taskStatus("PENDING")
                        .decisionStatus("PENDING")
                        .createdAt(LocalDateTime.now())
                        .build();
                taskRepository.save(firstTask);
                log.info("Created first approval task {} for role {} on workflow {}",
                        firstTask.getId(), first.getRole(), instance.getId());
            } else {
                log.warn("{} workflow template {} has no steps; no approval task created", entityType, template.getId());
            }
        } catch (Exception e) {
            log.error("Failed to process {} submission event for {}", entityType, entityId, e);
        }
    }
}
