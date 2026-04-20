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

@Document(collection = "workflow_instances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowInstance {
    
    @Id
    private UUID id;
    
    private UUID templateId;
    
    private String entityType; // LOAN, CERTIFICATE, MORTGAGE
    
    private UUID entityId; // ID of the loan/certificate/mortgage application
    
    private String status; // PENDING, IN_PROGRESS, APPROVED, REJECTED
    
    private int currentStep;
    
    private List<WorkflowStep> steps;
    
    private int version;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

