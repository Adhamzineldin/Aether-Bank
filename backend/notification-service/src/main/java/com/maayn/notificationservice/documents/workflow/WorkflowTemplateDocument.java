package com.maayn.notificationservice.documents.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import maayn.veld.generated.models.shared.Condition;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "workflow_templates")
@CompoundIndex(name = "entity_type_version_unique", def = "{'entityType': 1, 'version': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTemplateDocument {

    @Id
    private UUID id;

    private String entityType;
    private Integer version;

    private Condition condition;

    private List<WorkflowStepDocument> steps;
    private Boolean isActive;

    private String description;
    private UUID createdBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
