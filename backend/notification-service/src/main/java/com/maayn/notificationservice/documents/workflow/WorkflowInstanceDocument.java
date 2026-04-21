package com.maayn.notificationservice.documents.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import maayn.veld.generated.models.workflow.WorkflowStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "workflow_instances")
@CompoundIndex(name = "entity_type_id_unique", def = "{'entityType': 1, 'entityId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowInstanceDocument {
    @Id
    private UUID id;

    private UUID templateId;
    private Integer templateVersion;

    private String entityType;
    private UUID entityId;

    @Indexed
    private WorkflowStatus status;
    private String statusReason;

    private Integer currentStep;
    private List<WorkflowStepDocument> steps;

    @Version
    private Integer version;

    private LocalDateTime completedAt;
    private UUID completedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
