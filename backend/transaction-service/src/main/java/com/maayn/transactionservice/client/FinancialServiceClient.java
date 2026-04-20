package com.maayn.transactionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "financial-service", path = "/api/financial_service/fx")
public interface FinancialServiceClient {

    @GetMapping("/exchange-rate")
    FxRateResponse getExchangeRate(
            @RequestParam("sourceCurrency") String sourceCurrency,
            @RequestParam("destinationCurrency") String destinationCurrency
    );
}

class FxRateResponse {
    private String sourceCurrency;
    private String destinationCurrency;
    private BigDecimal exchangeRate;
    private String timestamp;

    // Getters and Setters
    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public String getDestinationCurrency() {
        return destinationCurrency;
    }

    public void setDestinationCurrency(String destinationCurrency) {
        this.destinationCurrency = destinationCurrency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

