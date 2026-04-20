package com.maayn.transactionservice.service;

import com.maayn.transactionservice.client.FinancialServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FxRateService {

    private final FinancialServiceClient financialServiceClient;

    // Fallback static rates in case Financial Service is unavailable
    private static final Map<String, BigDecimal> FALLBACK_RATES = Map.of(
            "USD_EUR", new BigDecimal("0.90"),
            "EUR_USD", new BigDecimal("1.11"),
            "USD_EGP", new BigDecimal("50.00"),
            "EGP_USD", new BigDecimal("0.02")
    );

    /**
     * Fetches live FX rate from Financial Service.
     * Falls back to cached rates if service is unavailable.
     */
    public BigDecimal getRate(String sourceCurrency, String destCurrency) {
        if (sourceCurrency.equals(destCurrency)) return BigDecimal.ONE;

        try {
            // Call Financial Service for live rate
            var response = financialServiceClient.getExchangeRate(sourceCurrency, destCurrency);
            log.info("Fetched live FX rate from Financial Service: {} -> {} = {}", 
                    sourceCurrency, destCurrency, response.getExchangeRate());
            return response.getExchangeRate();
        } catch (Exception e) {
            log.warn("Financial Service unavailable, using fallback rate for {}/{}: {}", 
                    sourceCurrency, destCurrency, e.getMessage());
            return getFallbackRate(sourceCurrency, destCurrency);
        }
    }

    private BigDecimal getFallbackRate(String sourceCurrency, String destCurrency) {
        String key = sourceCurrency + "_" + destCurrency;
        BigDecimal rate = FALLBACK_RATES.get(key);
        
        if (rate != null) {
            return rate;
        }

        // Try inverse rate
        String inverseKey = destCurrency + "_" + sourceCurrency;
        BigDecimal inverseRate = FALLBACK_RATES.get(inverseKey);
        if (inverseRate != null) {
            return BigDecimal.ONE.divide(inverseRate, 8, RoundingMode.HALF_UP);
        }

        throw new IllegalArgumentException("Unsupported currency pair: " + sourceCurrency + "/" + destCurrency);
    }

    /**
     * Calculates the final amount using Bankers Rounding (HALF_EVEN)
     */
    public BigDecimal calculateDestinationAmount(BigDecimal sourceAmount, BigDecimal rate) {
        return sourceAmount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
    }
}

