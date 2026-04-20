package com.maayn.notificationservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "workflow_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTemplate {
    
    @Id
    private UUID id;
    
    private String entityType; // e.g., "LOAN", "CERTIFICATE", "MORTGAGE"
    
    private List<WorkflowStep> steps;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

