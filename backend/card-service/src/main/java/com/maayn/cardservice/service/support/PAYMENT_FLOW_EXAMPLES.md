/**
 * PAYMENT FLOW INTEGRATION EXAMPLES
 * 
 * This document provides practical examples of using the payment flows
 * with SOLID principles and proper dependency injection.
 */

// Example 1: Process Credit Card Payment
// ========================================
// 
// @Inject
// private CardPaymentService cardPaymentService;
//
// public void processCreditCardPayment() throws Exception {
//     MerchantPaymentRequestDto request = MerchantPaymentRequestDto.builder()
//         .cardToken("credit-card-visa-token-123")
//         .merchantId(UUID.randomUUID().toString())
//         .iban("DE89370400440532013000")           // German merchant IBAN
//         .cvv("123")                               // Card security code
//         .expiryDate("12/25")                      // MM/YY format
//         .amount(BigDecimal.valueOf(150.00))
//         .currency("EUR")
//         .idempotencyKey("credit-payment-unique-key")
//         .build();
//     
//     request.validate();
//     
//     // Automatically routes to CreditCardPaymentFlow
//     // Features: Credit balance ledger, merchant IBAN routing
//     CardTransactionResponse response = cardPaymentService.process(request);
//     System.out.println("Payment processed: " + response.getReferenceNumber());
// }

// Example 2: Process Debit Card Payment
// ======================================
// 
// public void processDebitCardPayment() throws Exception {
//     MerchantPaymentRequestDto request = MerchantPaymentRequestDto.builder()
//         .cardToken("debit-card-mastercard-token-456")
//         .merchantId(UUID.randomUUID().toString())
//         .iban("ES9121000418450200051332")          // Spanish merchant IBAN
//         .cvv("456")                                // Card security code
//         .expiryDate("08/26")                       // MM/YY format
//         .amount(BigDecimal.valueOf(75.50))
//         .currency("EUR")
//         .idempotencyKey("debit-payment-unique-key")
//         .build();
//     
//     request.validate();
//     
//     // Automatically routes to DebitCardPaymentFlow
//     // Features: Immediate deduction, merchant IBAN routing
//     CardTransactionResponse response = cardPaymentService.process(request);
//     System.out.println("Payment processed: " + response.getReferenceNumber());
// }

// Example 3: Error Handling - Invalid IBAN
// =========================================
// 
// try {
//     MerchantPaymentRequestDto request = MerchantPaymentRequestDto.builder()
//         .cardToken("card-token")
//         .merchantId(UUID.randomUUID().toString())
//         .iban("DE89370400440532013001")           // WRONG checksum
//         .cvv("123")
//         .expiryDate("12/25")
//         .amount(BigDecimal.valueOf(100.00))
//         .currency("EUR")
//         .idempotencyKey("test-key")
//         .build();
//     
//     request.validate();
//     // Will throw: "IBAN checksum validation failed"
// } catch (IllegalArgumentException e) {
//     System.out.println("IBAN validation error: " + e.getMessage());
// }

// Example 4: Error Handling - Invalid CVV
// ========================================
// 
// try {
//     MerchantPaymentRequestDto request = MerchantPaymentRequestDto.builder()
//         // ... other fields ...
//         .cvv("ABC")                               // INVALID - must be numeric
//         // ... other fields ...
//         .build();
//     
//     request.validate();
//     // Will throw: "CVV must contain only digits"
// } catch (IllegalArgumentException e) {
//     System.out.println("CVV validation error: " + e.getMessage());
// }

// Example 5: Error Handling - Expired Card
// =========================================
// 
// try {
//     MerchantPaymentRequestDto request = MerchantPaymentRequestDto.builder()
//         // ... other fields ...
//         .expiryDate("01/20")                      // EXPIRED
//         // ... other fields ...
//         .build();
//     
//     request.validate();
//     // Will throw: "Card has expired"
// } catch (IllegalArgumentException e) {
//     System.out.println("Expiry validation error: " + e.getMessage());
// }

// Example 6: Idempotency and Retry Safety
// ========================================
// 
// String idempotencyKey = "unique-merchant-payment-2024-001";
// 
// // First attempt
// MerchantPaymentRequestDto request = MerchantPaymentRequestDto.builder()
//     .cardToken("card-token")
//     .merchantId(UUID.randomUUID().toString())
//     .iban("DE89370400440532013000")
//     .cvv("123")
//     .expiryDate("12/25")
//     .amount(BigDecimal.valueOf(100.00))
//     .currency("EUR")
//     .idempotencyKey(idempotencyKey)              // Same key
//     .build();
// 
// CardTransactionResponse response1 = cardPaymentService.process(request);
// System.out.println("First payment: " + response1.getReferenceNumber());
// 
// // Retry with same idempotency key - returns CACHED result
// CardTransactionResponse response2 = cardPaymentService.process(request);
// System.out.println("Retry: " + response2.getReferenceNumber());
// // response1.equals(response2) => TRUE (no duplicate payment)

// Example 7: SOLID Principles in Practice
// ========================================
// 
// Single Responsibility:
//   - IbanValidator: IBAN validation only
//   - CvvValidator: CVV validation only
//   - CreditCardPaymentFlow: Credit card processing only
//   - DebitCardPaymentFlow: Debit card processing only
// 
// Open/Closed:
//   - New payment flows can be added without changing existing code
//   - Implement PaymentFlow interface and register in factory
//   - Example: PrepaidCardPaymentFlow, VirtualCardPaymentFlow
// 
// Liskov Substitution:
//   - Both Credit and Debit flows implement PaymentFlow correctly
//   - Can be used interchangeably
//   - Contract maintained across implementations
// 
// Interface Segregation:
//   - PaymentFlow: Small, focused interface
//   - Validators: Single-purpose interfaces
//   - DTOs: Specific input contracts
// 
// Dependency Inversion:
//   - MerchantPaymentService depends on PaymentFlow abstraction
//   - Not on CreditCardPaymentFlow or DebitCardPaymentFlow
//   - Factory handles concrete creation
//   - @RequiredArgsConstructor enables clean DI

// Example 8: Dependency Injection Pattern
// ========================================
// 
// @Service
// @RequiredArgsConstructor  // ← Professional DI pattern
// public class CardPaymentService {
//     private final MerchantPaymentService merchantPaymentService;
//     private final CardRulesValidator cardRulesValidator;
//     // Spring automatically injects all dependencies
// }
// 
// @Component
// @RequiredArgsConstructor  // ← Professional DI pattern
// public class CreditCardPaymentFlow implements PaymentFlow {
//     private final TransactionGateway transactionGateway;
//     private final CardTransactionFactory cardTransactionFactory;
//     // Spring automatically injects all dependencies
// }

// Example 9: Factory Pattern for Payment Flow Selection
// ====================================================
// 
// Card card = cardRepository.findByToken(cardToken);
// 
// // Factory automatically selects the right flow
// PaymentFlow flow = paymentFlowFactory.createPaymentFlow(card);
// 
// if (card.getCardType() == CardType.CREDIT) {
//     // flow is CreditCardPaymentFlow (charges balance)
// } else if (card.getCardType() == CardType.DEBIT) {
//     // flow is DebitCardPaymentFlow (deducts immediately)
// }
// 
// // Same interface, different implementations
// response = flow.processPayment(card, merchantId, iban, cvv, expiryDate, ...);

// Example 10: Full Payment Processing Flow
// ========================================
// 
// 1. Request arrives at CardPaymentService
//    ↓
// 2. CardPaymentService validates and delegates to MerchantPaymentService
//    ↓
// 3. MerchantPaymentService retrieves card and creates payment flow
//    ↓
// 4. PaymentFlowFactory selects appropriate flow (Credit/Debit)
//    ↓
// 5. Selected flow processes payment:
//    - Validates IBAN, CVV, expiry date
//    - Processes through TransactionGateway
//    - Creates and persists transaction
//    - Applies charges (credit card only)
//    ↓
// 6. Response returned with reference number and status

package com.maayn.cardservice.service.support;
