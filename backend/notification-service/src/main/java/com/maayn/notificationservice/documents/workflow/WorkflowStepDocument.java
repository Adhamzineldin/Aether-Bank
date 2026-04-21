package com.maayn.notificationservice.documents.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import maayn.veld.generated.models.StepAction;
import maayn.veld.generated.models.StepRole;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStepDocument {
    @Id
    private String id;
    private Long step;
    private StepRole role;
    private StepAction action;
}
