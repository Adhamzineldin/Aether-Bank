package com.maayn.notificationservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStep {
    
    private UUID id;
    
    private int step;
    
    private String role; // RISK, MANAGER, DIRECTOR
    
    private String action; // APPROVE_LOAN, etc.
}

