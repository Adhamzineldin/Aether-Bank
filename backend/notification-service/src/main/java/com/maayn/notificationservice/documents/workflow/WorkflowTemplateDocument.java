package com.maayn.notificationservice.documents.workflow;

import com.maayn.notificationservice.dto.Condition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "workflow_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTemplateDocument {

    @Id
    private String id;

//    BOTH SHOULD BE UNIQUE
    private String entityType;
    private Long version;

    private Condition condition;

    private List<WorkflowStepDocument> steps;
    private Boolean isActive;

//    Some Metadata
    private String description;
    private String createdBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
