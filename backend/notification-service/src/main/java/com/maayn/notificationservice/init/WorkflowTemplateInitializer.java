package com.maayn.notificationservice.init;

import com.maayn.notificationservice.entity.WorkflowStep;
import com.maayn.notificationservice.entity.WorkflowTemplate;
import com.maayn.notificationservice.repository.WorkflowTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowTemplateInitializer implements CommandLineRunner {

    private final WorkflowTemplateRepository templateRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if templates already exist
        if (templateRepository.count() > 0) {
            log.info("Workflow templates already initialized");
            return;
        }

        log.info("Initializing workflow templates...");

        // Create LOAN workflow template (3-step approval)
        WorkflowTemplate loanTemplate = WorkflowTemplate.builder()
                .id(UUID.randomUUID())
                .entityType("LOAN")
                .steps(Arrays.asList(
                        WorkflowStep.builder()
                                .id(UUID.randomUUID())
                                .step(1)
                                .role("RISK")
                                .action("APPROVE_LOAN")
                                .build(),
                        WorkflowStep.builder()
                                .id(UUID.randomUUID())
                                .step(2)
                                .role("MANAGER")
                                .action("APPROVE_LOAN")
                                .build(),
                        WorkflowStep.builder()
                                .id(UUID.randomUUID())
                                .step(3)
                                .role("DIRECTOR")
                                .action("APPROVE_LOAN")
                                .build()
                ))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        templateRepository.save(loanTemplate);
        log.info("Created LOAN workflow template with 3 approval steps");

        // Create CERTIFICATE workflow template (2-step approval)
        WorkflowTemplate certificateTemplate = WorkflowTemplate.builder()
                .id(UUID.randomUUID())
                .entityType("CERTIFICATE")
                .steps(Arrays.asList(
                        WorkflowStep.builder()
                                .id(UUID.randomUUID())
                                .step(1)
                                .role("MANAGER")
                                .action("APPROVE_CERTIFICATE")
                                .build(),
                        WorkflowStep.builder()
                                .id(UUID.randomUUID())
                                .step(2)
                                .role("DIRECTOR")
                                .action("APPROVE_CERTIFICATE")
                                .build()
                ))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        templateRepository.save(certificateTemplate);
        log.info("Created CERTIFICATE workflow template with 2 approval steps");

        // Create MORTGAGE workflow template (3-step approval)
        WorkflowTemplate mortgageTemplate = WorkflowTemplate.builder()
                .id(UUID.randomUUID())
                .entityType("MORTGAGE")
                .steps(Arrays.asList(
                        WorkflowStep.builder()
                                .id(UUID.randomUUID())
                                .step(1)
                                .role("RISK")
                                .action("APPROVE_MORTGAGE")
                                .build(),
                        WorkflowStep.builder()
                                .id(UUID.randomUUID())
                                .step(2)
                                .role("MANAGER")
                                .action("APPROVE_MORTGAGE")
                                .build(),
                        WorkflowStep.builder()
                                .id(UUID.randomUUID())
                                .step(3)
                                .role("DIRECTOR")
                                .action("APPROVE_MORTGAGE")
                                .build()
                ))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        templateRepository.save(mortgageTemplate);
        log.info("Created MORTGAGE workflow template with 3 approval steps");

        log.info("Workflow templates initialization complete");
    }
}

