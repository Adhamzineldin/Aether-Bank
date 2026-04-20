package com.maayn.accountservice.client;

import com.maayn.accountservice.dto.BalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "transaction-service", path = "/api/transaction_service/ledger")
public interface TransactionServiceClient {

    @GetMapping("/{accountId}/{currency}/balance")
    BalanceResponse getAccountBalance(@PathVariable UUID accountId, @PathVariable String currency);
}

