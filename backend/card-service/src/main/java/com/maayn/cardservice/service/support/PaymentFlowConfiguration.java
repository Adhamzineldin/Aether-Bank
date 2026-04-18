package com.maayn.cardservice.service.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Payment Flow Configuration (SOLID: Dependency Inversion).
 * Spring configuration for payment flow beans and factory registration.
 * Enables clean dependency injection throughout the application.
 */
@Configuration
public class PaymentFlowConfiguration {

    /**
     * Registers payment flow beans.
     * Spring automatically discovers @Component beans in service.support package.
     */
    @Bean
    public Map<PaymentFlowType, PaymentFlow> paymentFlowRegistry(
            CreditCardPaymentFlow creditCardPaymentFlow,
            DebitCardPaymentFlow debitCardPaymentFlow) {
        Map<PaymentFlowType, PaymentFlow> registry = new HashMap<>();
        registry.put(PaymentFlowType.CREDIT_CARD, creditCardPaymentFlow);
        registry.put(PaymentFlowType.DEBIT_CARD, debitCardPaymentFlow);
        return registry;
    }
}
