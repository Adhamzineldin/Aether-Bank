package com.maayn.notificationservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "approval_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalTask {
    
    @Id
    private UUID id;
    
    private UUID workflowId;
    
    private int step;
    
    private String role; // RISK, MANAGER, DIRECTOR
    
    private UUID assignedTo; // Employee ID
    
    private String taskStatus; // PENDING, IN_PROGRESS, COMPLETED
    
    private String decisionStatus; // PENDING, APPROVED, REJECTED
    
    private UUID decidedBy;
    
    private LocalDateTime decisionAt;
    
    private String decisionComment;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;
}

