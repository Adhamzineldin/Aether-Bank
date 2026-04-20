package com.maayn.financialservice.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class MortgageApplicationResponse {
    private UUID applicationId;
    private String status;
    private String mortgageNumber;
    private LocalDateTime submittedAt;

    public MortgageApplicationResponse() {}

    public MortgageApplicationResponse(UUID applicationId, String status, String mortgageNumber, LocalDateTime submittedAt) {
        this.applicationId = applicationId;
        this.status = status;
        this.mortgageNumber = mortgageNumber;
        this.submittedAt = submittedAt;
    }

    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMortgageNumber() { return mortgageNumber; }
    public void setMortgageNumber(String mortgageNumber) { this.mortgageNumber = mortgageNumber; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
