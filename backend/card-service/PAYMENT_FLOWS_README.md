# Payment Flows Implementation - Card Service

## Overview

This document describes the implementation of **2 payment flows** (Credit and Debit card payments) for the Card Service in the Aether Bank project. The implementation follows **SOLID principles** and uses **dependency injection** throughout.

## Architecture

### Components

#### 1. **PaymentFlow Interface** (Interface Segregation - SOLID)
```
PaymentFlow
├── processPayment()
└── getFlowType()
```

#### 2. **Payment Flow Implementations** (Single Responsibility - SOLID)

**CreditCardPaymentFlow**
- Handles credit card payment processing
- Maintains internal credit balance ledger
- Routes payment through merchant vault account
- Key method: `processPayment(card, merchantId, iban, cvv, expiryDate, amount, currency, idempotencyKey)`

**DebitCardPaymentFlow**
- Handles debit card payment processing
- Immediate fund deduction
- Routes payment through merchant vault account
- Key method: `processPayment(card, merchantId, iban, cvv, expiryDate, amount, currency, idempotencyKey)`

#### 3. **Validators** (Single Responsibility - SOLID)

**IbanValidator**
- Validates IBAN format and length (15-34 characters)
- Validates IBAN checksum using mod-97 algorithm
- Masks IBAN in logs for security

**CvvValidator**
- Validates CVV/CVC format
- Accepts 3 digits (standard) or 4 digits (American Express)
- Only numeric validation

**ExpiryDateValidator**
- Validates expiry date format (MM/YY)
- Ensures card is not expired
- Prevents payment with expired cards

**MerchantPaymentValidator**
- Composite validator orchestrating IBAN, CVV, and expiry validation
- Validates card status and blocking state
- Single integration point for all payment validations

#### 4. **Factory Pattern** (Open/Closed - SOLID)

**PaymentFlowFactory**
- Creates appropriate payment flow based on card type
- Extensible design for new payment types
- Uses switch expression for clean factory logic

#### 5. **Services** (Dependency Inversion - SOLID)

**MerchantPaymentService**
- Orchestrates payment flow selection and processing
- Depends on abstractions (PaymentFlow) not concretions
- Handles card retrieval and payment delegation

**CardPaymentService** (Refactored)
- Uses `@RequiredArgsConstructor` for proper dependency injection
- Delegates payment processing to `MerchantPaymentService`
- Maintains idempotency key caching for retries

#### 6. **Data Transfer Objects** (Interface Segregation - SOLID)

**MerchantPaymentRequestDto**
- Clean separation of concerns for API input
- Includes validation method
- Contains all merchant payment parameters: IBAN, CVV, expiry

#### 7. **Configuration** (Dependency Inversion - SOLID)

**PaymentFlowConfiguration**
- Spring Bean configuration for payment flows
- Registers payment flow registry map
- Enables auto-wiring of all components

## Payment Flow Diagram

```
CardPaymentService
    ↓
MerchantPaymentService
    ↓
PaymentFlowFactory
    ├─→ CreditCardPaymentFlow (if card.type == CREDIT)
    └─→ DebitCardPaymentFlow (if card.type == DEBIT)
        ↓
        ├─ MerchantPaymentValidator (IBAN + CVV + ExpiryDate)
        ├─ TransactionGateway (process transfer)
        ├─ CardTransactionFactory (create transaction)
        ├─ CreditBalanceService (for credit cards only)
        └─ CardTransactionRepository (persist transaction)
```

## SOLID Principles Implementation

### ✅ Single Responsibility Principle
- Each class has one reason to change
- Validators handle specific validations
- Payment flows handle specific card types
- Services handle orchestration

### ✅ Open/Closed Principle
- PaymentFlowFactory is open for extension (new card types)
- Closed for modification (factory pattern)
- Add new payment flows without changing existing code

### ✅ Liskov Substitution Principle
- CreditCardPaymentFlow and DebitCardPaymentFlow implement PaymentFlow
- Both can be used interchangeably
- Contract maintained across implementations

### ✅ Interface Segregation Principle
- PaymentFlow interface is small and focused
- MerchantPaymentRequestDto separated from response
- Validators have single validation purpose

### ✅ Dependency Inversion Principle
- Services depend on abstractions (PaymentFlow interface)
- Not on concrete implementations
- Factory handles concrete object creation
- `@RequiredArgsConstructor` ensures proper DI

## File Structure

```
card-service/src/main/java/com/maayn/cardservice/
├── service/support/
│   ├── PaymentFlow.java                    (Interface)
│   ├── PaymentFlowType.java               (Enum)
│   ├── CreditCardPaymentFlow.java         (Implementation)
│   ├── DebitCardPaymentFlow.java          (Implementation)
│   ├── PaymentFlowFactory.java            (Factory)
│   ├── MerchantPaymentService.java        (Orchestrator)
│   ├── IbanValidator.java                 (Validator)
│   ├── CvvValidator.java                  (Validator)
│   ├── ExpiryDateValidator.java           (Validator)
│   ├── MerchantPaymentValidator.java      (Composite Validator)
│   ├── MerchantPaymentRequestDto.java     (DTO)
│   └── PaymentFlowConfiguration.java      (Spring Config)
├── service/usecase/
│   └── CardPaymentService.java            (Refactored - uses new flows)
└── entity/
    ├── Card.java                           (existing)
    └── CardTransaction.java                (existing)
```

## Key Features

### 1. **IBAN Validation**
- Format validation (country code + check digits + account)
- Length validation (15-34 characters)
- Checksum validation using mod-97 algorithm
- Masked logging for security

### 2. **CVV Validation**
- Format validation (numeric only)
- Length validation (3 or 4 digits)
- Prevents invalid card verification

### 3. **Expiry Date Validation**
- Format validation (MM/YY)
- Prevents expired card usage
- Real-time expiry checking

### 4. **Payment Flow Strategy**
- Runtime selection of payment flow
- Credit card: applies charge to balance
- Debit card: immediate fund deduction
- Extensible for future payment methods

### 5. **Idempotency**
- Cached transaction retrieval
- Prevents duplicate payments
- Deterministic key generation for retries

### 6. **Clean Code**
- Dependency injection with `@RequiredArgsConstructor`
- Proper logging with Slf4j
- Clear separation of concerns
- No unused imports

## Usage Example

```java
@Inject
private CardPaymentService cardPaymentService;

// Process merchant payment
MerchantPaymentRequest request = new MerchantPaymentRequest();
request.setCardToken("visa-token-123");
request.setMerchantId(UUID.fromString("..."));
request.setIban("DE89370400440532013000");
request.setCvv("123");
request.setExpiryDate("12/25");
request.setAmount(BigDecimal.valueOf(100.00));
request.setCurrency("EUR");
request.setIdempotencyKey("unique-key-123");

// Automatically routes to CreditCardPaymentFlow or DebitCardPaymentFlow
CardTransactionResponse response = cardPaymentService.process(request);
```

## Testing Considerations

### Unit Tests
- PaymentFlow implementations
- Individual validators
- Factory pattern behavior
- DTO validation

### Integration Tests
- End-to-end payment processing
- Credit vs Debit flow differentiation
- Idempotency key handling
- Transaction persistence

### Security Tests
- IBAN checksum validation
- CVV format validation
- Expired card rejection
- Masked sensitive data in logs

## Future Enhancements

1. **Additional Card Types**: Implement new PaymentFlow for prepaid, virtual cards
2. **Dynamic Merchant Routing**: Route based on merchant configuration
3. **Rate Limiting**: Add rate limiting per card
4. **Fraud Detection**: Integrate fraud detection service
5. **Payment Schedules**: Recurring/scheduled payments
6. **Multi-currency Support**: Enhanced FX rate handling

## SOLID Principles Checklist

- ✅ Single Responsibility: Each class has one reason to change
- ✅ Open/Closed: Factory pattern enables extension
- ✅ Liskov Substitution: Flows implement common interface correctly
- ✅ Interface Segregation: Small focused interfaces
- ✅ Dependency Inversion: Depends on abstractions via factory and DI

## Dependency Injection Patterns Used

1. **Constructor Injection** (@RequiredArgsConstructor)
2. **Component Scanning** (@Component, @Service)
3. **Factory Pattern** (PaymentFlowFactory)
4. **Bean Configuration** (@Bean, @Configuration)
5. **Transactional Management** (@Transactional)
