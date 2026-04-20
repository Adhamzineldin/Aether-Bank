package com.maayn.financialservice.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class CertificateApplicationResponse {
    private UUID applicationId;
    private String status;
    private String certificateNumber;
    private LocalDateTime submittedAt;

    public CertificateApplicationResponse() {}

    public CertificateApplicationResponse(UUID applicationId, String status, String certificateNumber, LocalDateTime submittedAt) {
        this.applicationId = applicationId;
        this.status = status;
        this.certificateNumber = certificateNumber;
        this.submittedAt = submittedAt;
    }

    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
