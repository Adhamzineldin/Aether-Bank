package com.maayn.transactionservice.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FxRateService {

    /**
     * TODO: Wire this to a real FX Rate Service (e.g. call an external FX microservice or Bloomberg/Reuters API).
     *       For now, using hardcoded static rates. Replace this method body with a service call when ready.
     *
     * Expected input:  sourceCurrency (String, e.g. "USD"), destinationCurrency (String, e.g. "EUR")
     * Expected output: BigDecimal exchange rate (e.g. 0.90 means 1 USD = 0.90 EUR)
     */
    public BigDecimal getRate(String sourceCurrency, String destCurrency) {
        if (sourceCurrency.equals(destCurrency)) return BigDecimal.ONE;

        // Static rates — replace with real FX service call
        if (sourceCurrency.equals("USD") && destCurrency.equals("EUR")) return new BigDecimal("0.90");
        if (sourceCurrency.equals("EUR") && destCurrency.equals("USD")) return new BigDecimal("1.11");
        if (sourceCurrency.equals("USD") && destCurrency.equals("EGP")) return new BigDecimal("50.00");
        if (sourceCurrency.equals("EGP") && destCurrency.equals("USD")) return new BigDecimal("0.02");

        throw new IllegalArgumentException("Unsupported currency pair: " + sourceCurrency + "/" + destCurrency);
    }

    /**
     * Calculates the final amount using Bankers Rounding (HALF_EVEN)
     */
    public BigDecimal calculateDestinationAmount(BigDecimal sourceAmount, BigDecimal rate) {
        return sourceAmount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
    }
}

