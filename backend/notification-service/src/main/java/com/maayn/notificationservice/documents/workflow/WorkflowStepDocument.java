package com.maayn.notificationservice.documents.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import maayn.veld.generated.models.shared.StepAction;
import maayn.veld.generated.models.workflow.StepRole;

import java.util.UUID;

import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStepDocument {
    @Id
    private UUID id;
    private Integer step;
    private StepRole role;
    private StepAction action;
}
