package com.maayn.notificationservice.documents.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import org.springframework.data.annotation.Id;

/**
 * Mongo representation of a single step inside a workflow template / instance.
 *
 * {@code role} and {@code action} are stored as free-form strings (e.g.
 * {@code "RISK"}, {@code "APPROVE_LOAN"}). The generated SDK does not currently
 * expose enum types for these values and the rest of the service
 * (entity {@code WorkflowStep}, {@code WorkflowTemplateController.StepInput}) is
 * already modeled on strings, so we match that to avoid lossy conversions at
 * the mapping layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStepDocument {
    @Id
    private UUID id;
    private Integer step;
    private String role;
    private String action;
}
