package com.maayn.financialservice.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class FxRateService {

    private static final Map<String, BigDecimal> RATES = Map.of(
            rateKey("USD", "EUR"), new BigDecimal("0.92"),
            rateKey("USD", "EGP"), new BigDecimal("48.50"),
            rateKey("EUR", "EGP"), new BigDecimal("52.80")
    );

    public BigDecimal getRate(String sourceCurrency, String destinationCurrency) {
        String source = normalize(sourceCurrency);
        String destination = normalize(destinationCurrency);
        if (source.equals(destination)) return BigDecimal.ONE;

        BigDecimal direct = RATES.get(rateKey(source, destination));
        if (direct != null) return direct;

        BigDecimal inverse = RATES.get(rateKey(destination, source));
        if (inverse != null) return BigDecimal.ONE.divide(inverse, 8, RoundingMode.HALF_UP);

        throw new IllegalArgumentException("Unsupported currency pair: " + source + " -> " + destination);
    }

    public BigDecimal convert(BigDecimal amount, String sourceCurrency, String destinationCurrency) {
        return amount.multiply(getRate(sourceCurrency, destinationCurrency)).setScale(2, RoundingMode.HALF_UP);
    }

    private static String rateKey(String source, String destination) {
        return normalize(source) + "_" + normalize(destination);
    }

    private static String normalize(String currency) {
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("Currency code is required");
        return currency.trim().toUpperCase();
    }
}
