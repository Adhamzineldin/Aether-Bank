package com.maayn.notificationservice.documents.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Mongo representation of a workflow template.
 *
 * {@code condition} is kept as a free-form string expression for now (there is
 * no shared enum/type in the generated SDK). When the product decides on a
 * structured condition DSL we can upgrade this field.
 */
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

    private String condition;

    private List<WorkflowStepDocument> steps;
    private Boolean isActive;

    private String description;
    private UUID createdBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
