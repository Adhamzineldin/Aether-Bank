# 🚀 Quick Reference Guide - Payment Flows

## File Locations

### Core Implementation
```
card-service/src/main/java/com/maayn/cardservice/service/support/
├── PaymentFlow.java                    (Interface)
├── PaymentFlowType.java               (Enum)
├── CreditCardPaymentFlow.java         (Credit implementation)
├── DebitCardPaymentFlow.java          (Debit implementation)
├── PaymentFlowFactory.java            (Factory)
├── MerchantPaymentService.java        (Orchestrator)
├── IbanValidator.java                 (IBAN validation)
├── CvvValidator.java                  (CVV validation)
├── ExpiryDateValidator.java           (Expiry validation)
├── MerchantPaymentValidator.java      (Composite validator)
├── MerchantPaymentRequestDto.java     (DTO)
└── PaymentFlowConfiguration.java      (Spring config)

card-service/src/main/java/com/maayn/cardservice/service/usecase/
└── CardPaymentService.java            (Refactored entry point)
```

### Tests
```
card-service/src/test/java/com/maayn/cardservice/
├── PaymentValidatorsTest.java         (Validator tests)
└── PaymentFlowFactoryTest.java        (Factory tests)
```

### Documentation
```
card-service/
├── PAYMENT_FLOWS_README.md            (Comprehensive guide)
├── IMPLEMENTATION_SUMMARY.md          (Summary & checklist)
├── COMPLETION_CHECKLIST.md            (Verification checklist)
└── src/main/java/.../PAYMENT_FLOW_EXAMPLES.md (Code examples)
```

---

## Quick Usage

### 1. Inject Dependencies (Professional DI)
```java
@Service
@RequiredArgsConstructor
public class YourService {
    private final CardPaymentService cardPaymentService;
}
```

### 2. Create Payment Request
```java
MerchantPaymentRequestDto request = MerchantPaymentRequestDto.builder()
    .cardToken("visa-token-123")
    .merchantId(UUID.randomUUID().toString())
    .iban("DE89370400440532013000")
    .cvv("123")
    .expiryDate("12/25")
    .amount(BigDecimal.valueOf(100.00))
    .currency("EUR")
    .idempotencyKey("unique-key")
    .build();

request.validate();  // Validates all fields
```

### 3. Process Payment
```java
CardTransactionResponse response = cardPaymentService.process(request);
// Automatically routes to CreditCardPaymentFlow or DebitCardPaymentFlow
```

---

## Validation Examples

### IBAN Validation
```java
// Valid IBAN
"DE89370400440532013000"  ✅

// Invalid (wrong checksum)
"DE89370400440532013001"  ❌

// Invalid (wrong format)
"XX12345678901234567890"  ❌
```

### CVV Validation
```java
// Valid
"123"    ✅  (3 digits)
"1234"   ✅  (4 digits - AmEx)

// Invalid
"AB"     ❌  (non-numeric)
"12345"  ❌  (too long)
```

### Expiry Date Validation
```java
// Valid
"12/25"  ✅  (future)

// Invalid
"01/20"  ❌  (expired)
"2025-12" ❌ (wrong format)
```

---

## Payment Flow Selection

### Automatic Routing
```
Card Type: CREDIT  →  CreditCardPaymentFlow
                       - Charge to balance
                       - Merchant IBAN routing
                       
Card Type: DEBIT   →  DebitCardPaymentFlow
                       - Immediate deduction
                       - Merchant IBAN routing
```

---

## SOLID Principles - Quick Reference

| Principle | Implementation | Benefit |
|-----------|----------------|---------|
| **S** - Single Responsibility | Each validator/flow has one job | Easy to maintain & test |
| **O** - Open/Closed | Factory pattern for new flows | Add features without changes |
| **L** - Liskov Substitution | Both flows implement same interface | Interchangeable implementations |
| **I** - Interface Segregation | Small focused interfaces | Clean contracts |
| **D** - Dependency Inversion | Depend on abstractions | Loose coupling, easy testing |

---

## Dependency Injection - Quick Reference

### Pattern Used
```java
@Component/@Service
@RequiredArgsConstructor  // ← Magic happens here
public class MyClass {
    private final DependencyA depA;  // Injected automatically
    private final DependencyB depB;  // Injected automatically
    // No constructor needed!
}
```

### Benefits
- ✅ No manual wiring
- ✅ Easy to test (mock injection)
- ✅ Spring handles lifecycle
- ✅ Clean, readable code
- ✅ Follows professional standards

---

## Validation Flow

```
Input Request
    ↓
1. Field Validation (required fields)
    ↓
2. IBAN Validation
    - Format check
    - Checksum validation
    ↓
3. CVV Validation
    - Numeric check
    - Length check
    ↓
4. Expiry Date Validation
    - Format check
    - Expiry check
    ↓
5. Card Status Validation
    - Status check
    - Blocking check
    ↓
Process Payment ✅
```

---

## Error Handling

### Common Validation Errors
```
"IBAN cannot be null or empty"
"IBAN checksum validation failed"
"IBAN length must be between 15 and 34 characters"
"CVV must contain only digits"
"CVV must be 3 or 4 digits"
"Expiry date must be in MM/YY format"
"Card has expired"
"Card is not active"
"Card is blocked"
```

---

## Idempotency

### Duplicate Prevention
```java
// First attempt
CardTransactionResponse response1 = cardPaymentService.process(request);

// Retry with SAME idempotency key
CardTransactionResponse response2 = cardPaymentService.process(request);

// Returns CACHED result - no duplicate payment!
response1.equals(response2)  // TRUE
```

---

## Testing Quick Commands

### Run All Tests
```bash
cd card-service
mvnw.cmd test
```

### Run Specific Test
```bash
mvnw.cmd test -Dtest=PaymentValidatorsTest
mvnw.cmd test -Dtest=PaymentFlowFactoryTest
```

### Build Project
```bash
mvnw.cmd clean compile
```

---

## Architecture Overview (ASCII)

```
┌─────────────────────┐
│ CardPaymentService  │ (Entry point)
└──────────┬──────────┘
           │
┌──────────▼─────────────────┐
│MerchantPaymentService      │ (Orchestrator)
└──────────┬─────────────────┘
           │
┌──────────▼──────────────────┐
│ PaymentFlowFactory          │ (Factory pattern)
└──────────┬──────────────────┘
           │
      ┌────┴─────┐
      │           │
┌─────▼──────┐ ┌─▼──────────┐
│ CreditFlow │ │ DebitFlow  │
└─────┬──────┘ └─┬──────────┘
      │          │
      └────┬─────┘
           │
    ┌──────▼────────────┐
    │ Validators:       │
    │ - IBAN            │
    │ - CVV             │
    │ - Expiry          │
    │ - Card Status     │
    └───────────────────┘
```

---

## Key Files to Know

| File | Purpose | Key Method |
|------|---------|-----------|
| PaymentFlow | Interface | processPayment() |
| CreditCardPaymentFlow | Credit processing | processPayment() |
| DebitCardPaymentFlow | Debit processing | processPayment() |
| PaymentFlowFactory | Flow selection | createPaymentFlow() |
| MerchantPaymentService | Orchestration | processMerchantPayment() |
| CardPaymentService | Entry point | process() |
| IbanValidator | IBAN check | validate() |
| CvvValidator | CVV check | validate() |
| ExpiryDateValidator | Expiry check | validate() |

---

## Configuration

### Spring Auto-Detects
```java
@Component  // Card flows
@Component  // Validators
@Service    // Services
@Configuration  // Bean configuration
```

### Manual Registration (if needed)
```java
@Bean
public PaymentFlow creditCardPaymentFlow(...) {
    return new CreditCardPaymentFlow(...);
}

@Bean
public PaymentFlow debitCardPaymentFlow(...) {
    return new DebitCardPaymentFlow(...);
}
```

---

## Security Considerations

### IBAN Masking
```java
"DE89370400440532013000"  →  "DE89****3000"  (in logs)
```

### CVV Not Stored
- Validated but not persisted
- Only reference stored

### Expiry Validation
- Real-time check
- Prevents expired card usage

---

## Next Steps

1. **Build**: Run `mvnw.cmd clean compile` to verify
2. **Test**: Run unit tests with `mvnw.cmd test`
3. **Integrate**: Use in your controllers/APIs
4. **Deploy**: Ready for production use

---

## Support Documentation

- 📖 **Full Guide**: `PAYMENT_FLOWS_README.md`
- 📋 **Summary**: `IMPLEMENTATION_SUMMARY.md`
- ✅ **Checklist**: `COMPLETION_CHECKLIST.md`
- 💡 **Examples**: `PAYMENT_FLOW_EXAMPLES.md`

---

## That's It! 🎉

You now have a professional-grade payment processing system with:
- ✅ 2 payment flows
- ✅ SOLID principles
- ✅ Professional DI
- ✅ Complete validation
- ✅ Full documentation
