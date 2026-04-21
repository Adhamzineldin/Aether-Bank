package com.maayn.notificationservice.documents.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import maayn.veld.generated.models.workflow.DecisionStatus;
import maayn.veld.generated.models.workflow.StepRole;
import maayn.veld.generated.models.workflow.TaskStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "approval_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalTaskDocument {

    @Id
    private UUID id;

    @Indexed
    private UUID workflowId;
    private Integer step;

    private StepRole role;
    private UUID assignedTo;

    private TaskStatus taskStatus;
    private DecisionStatus decisionStatus;

    private UUID decidedBy;
    private LocalDateTime decisionAt;
    private String decisionComment;

    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
