package com.maayn.financialservice.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CertificateIssueCommand(
        UUID customerId,
        UUID accountId,
        UUID applicationId,
        String productCode,
        BigDecimal principal,
        BigDecimal annualRate,
        LocalDate issueDate,
        LocalDate maturityDate
) {}
