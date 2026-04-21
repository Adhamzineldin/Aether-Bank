package com.maayn.notificationservice.documents.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import maayn.veld.generated.models.WorkflowStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "workflow_instances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowInstanceDocument {
    @Id
    private String id;

    private UUID templateId;
    private Long templateVersion;
//    Both must be unique
    private String entityType;
    @Indexed
    private UUID entityId;

    @Indexed
    private WorkflowStatus status;
    private String statusReason;

    private Long currentStep;
//    This should be removed i will check it now
    private List<WorkflowStepDocument> steps;

    @Version
    private Long version;

    private LocalDateTime completedAt;
    private UUID completedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
