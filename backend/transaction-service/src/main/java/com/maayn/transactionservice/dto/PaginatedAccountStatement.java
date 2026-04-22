package com.maayn.transactionservice.dto;

import java.util.List;

public record PaginatedAccountStatement(
        List<AccountStatementRow> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isLast
) {}
