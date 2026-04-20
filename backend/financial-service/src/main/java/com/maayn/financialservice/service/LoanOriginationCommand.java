package com.maayn.financialservice.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LoanOriginationCommand(
        UUID customerId,
        UUID accountId,
        UUID applicationId,
        String productCode,
        BigDecimal principal,
        Integer tenureMonths,
        LocalDate startDate,
        LocalDate maturityDate
) {}
