# COMPREHENSIVE BACKEND ARCHITECTURE PLAN
## Aether Bank - Complete Event-Driven Microservices System

---

## CURRENT STATE ANALYSIS

### ✅ COMPLETED SERVICES

#### 1. **Transaction Service** (COMPLETE & WELL-BUILT)
- ✅ Deposit, Withdraw, Transfer operations
- ✅ Ledger management with double-entry bookkeeping
- ✅ FX rate service integration (PLACEHOLDER - needs to connect to Financial Service)
- ✅ Event-driven with RabbitMQ
- ✅ Idempotency handling
- ✅ Outbox pattern for reliable messaging
- ✅ Proper entity design (Transaction, LedgerBalance, OutboxMessage)
- ✅ Comprehensive service layer architecture
- **STATUS**: Production Ready, needs FX integration fix

#### 2. **IAM Service** (COMPLETE)
- ✅ Authentication (Login/Register/Logout)
- ✅ JWT token generation
- ✅ Role-based access control (Admin, Employee, Customer)
- ✅ User management
- ✅ Audit logging
- ✅ OTP support
- ✅ Password hashing
- **STATUS**: Production Ready

#### 3. **Card Service** (COMPLETE)
- ✅ Card creation (Debit/Credit)
- ✅ Merchant payment processing
- ✅ Refund and void operations
- ✅ Card transaction history
- ✅ Payment gateway integration
- ✅ Vault routing for merchant/customer accounts
- ✅ Event-driven architecture
- **STATUS**: Production Ready

#### 4. **Financial Service** (PARTIAL - NEEDS COMPLETION)
- ✅ Loan application submission
- ✅ Certificate (CD) application submission
- ✅ FX rate service (complete implementation)
- ✅ Investment module (full VELD definition)
- ❌ **MISSING**: Loan approval workflow integration
- ❌ **MISSING**: Certificate management after submission
- ❌ **MISSING**: Mortgage module (mentioned in plan but not implemented)
- ❌ **MISSING**: Investment Java implementation
- **STATUS**: 60% Complete

#### 5. **Notification & Workflow Service** (SKELETON ONLY)
- ✅ VELD definitions complete (Workflow + Notification modules)
- ❌ **MISSING**: All Java implementations (returns null)
- ❌ **MISSING**: Workflow template management
- ❌ **MISSING**: Approval task routing
- ❌ **MISSING**: Event listeners for transaction/loan/card events
- ❌ **MISSING**: Email/SMS notification sending
- **STATUS**: 10% Complete (only structure)

#### 6. **Audit Service** (MINIMAL)
- ✅ Basic audit log entity
- ✅ Security audit listener
- ❌ **MISSING**: Comprehensive event logging
- ❌ **MISSING**: Query/reporting APIs
- **STATUS**: 30% Complete

---

## ❌ CRITICAL ISSUES IDENTIFIED

### 1. **Account Service is COMPLETELY BROKEN**
- Current implementation only has "Customer Profile" management
- **MISSING ENTIRELY**:
  - ✗ Bank account opening/closing
  - ✗ Account types (Checking/Savings/Investment)
  - ✗ Account status management
  - ✗ Account-to-customer relationship
  - ✗ Integration with Transaction Service
  - ✗ Account balance querying (relies on Transaction Service)
  - ✗ AccountCreatedEvent publishing
- **VELD Definition EXISTS** but NO Java implementation
- **SEVERITY**: CRITICAL - This is core banking functionality

### 2. **Transaction Service FX Integration**
- Transaction service has hardcoded FX rates
- Financial Service has complete FX service implementation
- **FIX NEEDED**: Transaction Service must call Financial Service for live rates

### 3. **Workflow Service Not Integrated**
- Loan applications submitted but no approval workflow
- No routing to employee dashboards
- No approval/rejection mechanism
- **IMPACT**: Loans stuck in SUBMITTED state forever

### 4. **Missing Event Consumers**
- Notification service should listen to:
  - TransferSuccessEvent → Send email
  - LoanApplicationSubmittedEvent → Create workflow
  - LoanApprovedEvent → Send notification
  - AccountCreatedEvent → Welcome email
- Currently NO event consumers except minimal LoanConsumer

---

## 🎯 REQUIRED IMPLEMENTATION (PRIORITY ORDER)

### PHASE 1: FIX ACCOUNT SERVICE (CRITICAL)
**Priority**: 🔥 URGENT
**Time**: 4-6 hours

#### A. Complete VELD Definitions
All VELD definitions already exist in `account.veld`, need to verify completeness:
- ✓ OpenAccount
- ✓ GetAccount  
- ✓ ListCustomerAccounts
- ✓ CloseAccount
- ✓ UpdateAccountStatus
- ✓ DoesAccountExist

#### B. Java Implementation Required
1. **Entity**: `BankAccount.java`
   - id, accountNumber, customerId, accountType, status, currency, openedDate, closedDate
   - Use enums: AccountType (CHECKING, SAVINGS, INVESTMENT), AccountStatus (PENDING, ACTIVE, FROZEN, CLOSED)

2. **Repository**: `BankAccountRepository.java`
   - findByAccountNumber
   - findByCustomerId
   - existsByAccountNumber

3. **Service**: `AccountService.java` (implements IAccountService from Veld)
   - openAccount(): Create account, publish AccountCreatedEvent to RabbitMQ
   - getAccount(): Query account + call Transaction Service ledger for balance
   - listCustomerAccounts(): Return all accounts for customer
   - closeAccount(): Mark CLOSED, transfer remaining balance
   - updateAccountStatus(): ACTIVE ↔ FROZEN transitions
   - doesAccountExist(): Quick existence check

4. **Event Publishing**:
   - AccountCreatedEvent → consumed by Transaction Service to initialize ledger
   - AccountClosedEvent → consumed by Notification Service

5. **Integration**:
   - Feign Client to Transaction Service: `GET /ledger/{accountId}/{currency}/balance`
   - Combine account metadata + balance in `AccountResponse`

#### C. Customer Profile Integration
- Keep existing Customer entity
- Link BankAccount.customerId → Customer.id (FK optional for microservices)
- Customer can have multiple BankAccounts

---

### PHASE 2: INTEGRATE TRANSACTION ↔ FINANCIAL (FX)
**Priority**: 🔥 HIGH
**Time**: 1-2 hours

#### Changes Required
1. **Transaction Service**:
   - Add Feign Client: `FinancialServiceClient`
   ```java
   @FeignClient(name = "financial-service")
   public interface FinancialServiceClient {
       @GetMapping("/api/financial_service/fx/exchange-rate")
       GetRatesResponse getExchangeRate(@RequestParam String sourceCurrency, 
                                        @RequestParam String destinationCurrency);
   }
   ```

2. **Replace FxRateService.java**:
   - Current: Hardcoded rates
   - New: Call Financial Service FX API
   - Keep calculation logic in Transaction Service

3. **Add Fallback**:
   - If Financial Service unavailable → use cached/default rates
   - Log warning

---

### PHASE 3: COMPLETE NOTIFICATION & WORKFLOW SERVICE
**Priority**: 🔥 HIGH  
**Time**: 6-8 hours

#### A. Workflow Module Implementation

1. **Entities**:
   - `WorkflowTemplate.java` (MongoDB document)
   - `WorkflowInstance.java` (MongoDB document)
   - `ApprovalTask.java` (MongoDB document)

2. **Repositories**:
   - `WorkflowTemplateRepository extends MongoRepository`
   - `WorkflowInstanceRepository extends MongoRepository`
   - `ApprovalTaskRepository extends MongoRepository`

3. **Service**: `WorkflowServiceImpl.java`
   - `createWorkflow()`: 
     - Create instance from template
     - Generate first ApprovalTask for RISK role
   - `getWorkflow()`: Retrieve by ID
   - `getWorkflows()`: List all (filter by role)
   - `getTasks()`: Get pending tasks for employee
   - `decideTask()`: APPROVE/REJECT
     - If APPROVED → create next task OR complete workflow
     - If REJECTED → mark workflow as REJECTED
     - Publish LoanApprovedEvent / LoanRejectedEvent

4. **Workflow Templates** (seed data):
   ```java
   WorkflowTemplate loanTemplate = {
     entityType: "LOAN",
     steps: [
       { step: 1, role: RISK, action: APPROVE_LOAN },
       { step: 2, role: MANAGER, action: APPROVE_LOAN },
       { step: 3, role: DIRECTOR, action: APPROVE_LOAN }
     ]
   }
   ```

#### B. Notification Module Implementation

1. **Entities**:
   - `NotificationItem.java` (MongoDB)
   - `NotificationTemplate.java` (MongoDB)

2. **Repositories**:
   - `NotificationRepository`
   - `NotificationTemplateRepository`

3. **Service**: `NotificationServiceImpl.java`
   - `sendNotification()`: Create notification record + send via channel
   - `listNotifications()`: Get user notifications
   - `retryNotification()`: Retry failed sends
   - Template CRUD operations

4. **Email Service**:
   - Use Spring Mail (already configured in EmailConfig)
   - Templates for: Welcome, Transfer, Loan Status, etc.

#### C. Event Consumers (RabbitMQ Listeners)

1. **TransactionEventConsumer.java**:
   ```java
   @RabbitListener(queues = "transaction.success")
   public void onTransferSuccess(TransferSuccessEvent event) {
       // Send email to source account owner
       // Send email to destination account owner
   }
   ```

2. **LoanApplicationConsumer.java**:
   ```java
   @RabbitListener(queues = "loan.submitted")
   public void onLoanSubmitted(LoanApplicationSubmittedEvent event) {
       // Create workflow instance
       workflowService.createWorkflow({
           entityType: "LOAN",
           entityId: event.loanId,
           templateId: LOAN_WORKFLOW_TEMPLATE_ID
       });
       // Send notification to applicant
   }
   ```

3. **WorkflowEventConsumer.java**:
   ```java
   @RabbitListener(queues = "loan.approved")
   public void onLoanApproved(LoanApprovedEvent event) {
       // Trigger Financial Service to disburse loan
       // Send success notification to customer
   }
   ```

4. **AccountEventConsumer.java**:
   ```java
   @RabbitListener(queues = "account.created")
   public void onAccountCreated(AccountCreatedEvent event) {
       // Send welcome email with account details
   }
   ```

---

### PHASE 4: COMPLETE FINANCIAL SERVICE
**Priority**: 🟡 MEDIUM
**Time**: 8-10 hours

#### A. Mortgage Module (Missing)
1. **VELD Definition**: Create `mortgage.veld`
   ```veld
   module Mortgage {
     prefix: /mortgages
     
     action submitMortgageApplication {
       method: POST
       path: /application
       input: MortgageApplication
       output: MortgageApplicationResponse
     }
     
     action getMortgageDetails {
       method: GET
       path: /:mortgageId
       output: Mortgage
     }
   }
   ```

2. **Java Implementation**:
   - Entity: `MortgageApplicationDocument.java` (MongoDB)
   - Repository: `MortgageRepo.java`
   - Service: `MortgageService.java implements IMortgageService`
   - Similar structure to LoanService

#### B. Investment Module (Java Implementation)
VELD already exists, need Java:
1. **Entities**:
   - `InvestmentAccount.java`
   - `AssetHolding.java`
   - `Asset.java`
   - `InvestmentTransaction.java`

2. **Repositories**:
   - `InvestmentAccountRepository`
   - `AssetHoldingRepository`
   - `AssetRepository`

3. **Service**: `InvestmentService.java implements IInvestmentService`
   - openInvestmentAccount()
   - buyAsset(): Deduct from linked BankAccount, create holding
   - sellAsset(): Credit to BankAccount, reduce holding
   - getPortfolio(): Calculate current valuation
   - getPerformance(): ROI, gains/losses

4. **Integration**:
   - Feign Client to Account Service (check balance)
   - Feign Client to Transaction Service (execute transfers)
   - External API for real-time asset prices (mock initially)

#### C. Loan Workflow Integration
1. **Event Listener** in Financial Service:
   ```java
   @RabbitListener(queues = "loan.approved")
   public void disburseLoan(LoanApprovedEvent event) {
       // Update loan status to DISBURSED
       // Call Transaction Service to transfer funds
       // Publish LoanDisbursedEvent
   }
   ```

2. **Loan Management APIs**:
   - getLoanDetails(loanId)
   - listCustomerLoans(customerId)
   - makeRepayment(loanId, amount)
   - getLoanSchedule(loanId)

---

### PHASE 5: ENHANCE AUDIT SERVICE
**Priority**: 🟢 LOW
**Time**: 3-4 hours

#### Implementation Required:
1. **Event Consumers** for ALL services:
   - Listen to every business event
   - Store in AuditLog with metadata

2. **Query APIs** (add to VELD):
   ```veld
   module Audit {
     action getAuditLogs {
       method: GET
       path: /logs
       output: AuditLog[]
     }
     
     action getAuditLog {
       method: GET
       path: /logs/:id
       output: AuditLog
     }
   }
   ```

3. **Service Implementation**:
   - `AuditService.java implements IAuditService`
   - Filter by service, user, date range, action type

---

## 📋 VELD UPDATES REQUIRED

### 1. Account Service VELD (Already Complete)
Located at: `veld/services/account/modules/account.veld`
- ✓ All actions defined
- Action: **Verify and regenerate Veld code**

### 2. Financial Service VELD
Add mortgage module:
```veld
// veld/services/financial/modules/mortgage.veld
import "../models/mortgage.model.veld"

module Mortgage {
  prefix: /mortgages
  
  action submitApplication {
    method: POST
    path: /application
    input: MortgageApplication
    output: MortgageApplicationResponse
  }
  
  action getMortgage {
    method: GET
    path: /:mortgageId
    output: Mortgage
  }
  
  action listCustomerMortgages {
    method: GET
    path: /customer/:customerId
    output: Mortgage[]
  }
}
```

Add mortgage model:
```veld
// veld/services/financial/models/mortgage.model.veld
model MortgageApplication {
  customerId: uuid
  propertyAddress: string
  propertyValue: decimal
  downPayment: decimal
  requestedAmount: decimal
  termYears: int
  employmentStatus: EmploymentStatus
  annualIncome: decimal
  creditScore: int
}

model MortgageApplicationResponse {
  applicationId: uuid
  status: ApplicationStatus
  submittedAt: datetime
}

model Mortgage {
  id: uuid
  mortgageNumber: string
  applicationId: uuid
  customerId: uuid
  accountId: uuid
  propertyAddress: string
  propertyValue: decimal
  principalAmount: decimal
  interestRate: decimal
  termMonths: int
  monthlyPayment: decimal
  outstandingBalance: decimal
  status: LoanStatus
  startDate: date
  endDate: date
  createdAt: datetime
}
```

### 3. Notification Service Events
Add event models:
```veld
// veld/services/notification/models/events.model.veld
model TransferSuccessEvent {
  transactionId: string
  sourceAccountId: uuid
  destinationAccountId: uuid
  amount: decimal
  currency: string
  timestamp: datetime
}

model LoanApplicationSubmittedEvent {
  loanId: uuid
  customerId: uuid
  amount: decimal
  timestamp: datetime
}

model LoanApprovedEvent {
  loanId: uuid
  customerId: uuid
  approvedAmount: decimal
  timestamp: datetime
}

model AccountCreatedEvent {
  accountId: uuid
  customerId: uuid
  accountType: string
  timestamp: datetime
}
```

### 4. Audit Service VELD (Missing)
Create new file:
```veld
// veld/services/audit/main.veld
prefix: /api/audit_service

import "@models/*"
import "@modules/*"
```

```veld
// veld/services/audit/modules/audit.veld
import "../models/audit.model.veld"

module Audit {
  prefix: /audit
  
  action getAuditLogs {
    method: GET
    path: /logs
    output: AuditLog[]
  }
  
  action getAuditLog {
    method: GET
    path: /logs/:id
    output: AuditLog
  }
  
  action getAuditsByUser {
    method: GET
    path: /logs/user/:userId
    output: AuditLog[]
  }
}
```

---

## 🔄 EVENT-DRIVEN ARCHITECTURE MAP

### Event Publishers:
1. **Account Service**:
   - `AccountCreatedEvent` → transaction.account.created
   - `AccountClosedEvent` → transaction.account.closed

2. **Transaction Service**:
   - `TransferSuccessEvent` → transaction.success
   - `TransferFailedEvent` → transaction.failed

3. **Financial Service**:
   - `LoanApplicationSubmittedEvent` → loan.submitted
   - `LoanDisbursedEvent` → loan.disbursed
   - `CertificateIssuedEvent` → certificate.issued

4. **Notification Service** (Workflow):
   - `LoanApprovedEvent` → loan.approved
   - `LoanRejectedEvent` → loan.rejected

### Event Consumers:
1. **Transaction Service**:
   - Listens: `AccountCreatedEvent` → Initialize ledger

2. **Notification Service**:
   - Listens: ALL events → Send notifications

3. **Audit Service**:
   - Listens: ALL events → Log to audit trail

4. **Financial Service**:
   - Listens: `LoanApprovedEvent` → Disburse funds

---

## 🏗️ MICROSERVICES FINAL STRUCTURE

### Service 1: IAM Service ✅
- **Database**: PostgreSQL
- **Responsibilities**: Auth, Users, Roles, JWT
- **Status**: COMPLETE

### Service 2: Account Service ❌➡️✅ (TO BE FIXED)
- **Database**: PostgreSQL
- **Responsibilities**: Bank Accounts, Customer Profiles
- **Integration**: Transaction Service (balance queries)
- **Status**: NEEDS COMPLETE REBUILD

### Service 3: Transaction Service ✅➡️✅ (MINOR FIX)
- **Database**: PostgreSQL
- **Responsibilities**: Transfers, Deposits, Withdrawals, Ledger
- **Integration**: Financial Service (FX rates)
- **Status**: NEEDS FX INTEGRATION

### Service 4: Financial Service 🟡➡️✅ (MAJOR COMPLETION)
- **Database**: MongoDB
- **Responsibilities**: Loans, Mortgages, Certificates, Investments, FX
- **Status**: NEEDS MORTGAGE + INVESTMENT + WORKFLOW INTEGRATION

### Service 5: Card Service ✅
- **Database**: PostgreSQL
- **Responsibilities**: Cards, Payments, Gateway
- **Status**: COMPLETE

### Service 6: Notification & Workflow Service ❌➡️✅ (CRITICAL)
- **Database**: MongoDB
- **Responsibilities**: Notifications, Approvals, Workflows
- **Status**: NEEDS FULL IMPLEMENTATION

### Service 7: Audit Service 🟡➡️✅ (ENHANCEMENT)
- **Database**: Cassandra (or MongoDB)
- **Responsibilities**: Centralized Logging
- **Status**: NEEDS EVENT CONSUMERS + APIS

---

## 🎯 IMPLEMENTATION ORDER (STRICT SEQUENCE)

### Week 1: Critical Path
1. **Day 1-2**: Fix Account Service (VELD + Java + Integration)
2. **Day 3**: Integrate Transaction ↔ Financial FX
3. **Day 4-5**: Implement Workflow Service (Core logic)
4. **Day 6-7**: Implement Notification Service (Event consumers)

### Week 2: Completions
5. **Day 1-2**: Financial Service Mortgage module
6. **Day 3-4**: Financial Service Investment module
7. **Day 5**: Loan disbursement integration
8. **Day 6-7**: Audit Service enhancement + testing

---

## 🧪 TESTING CHECKLIST

### Integration Tests Required:
- [ ] Account Service: Create account → Transaction Service initializes ledger
- [ ] Transaction Service: Transfer USD→EUR → Calls Financial FX service
- [ ] Financial Service: Loan submitted → Workflow created in Notification Service
- [ ] Notification Service: Loan approved → Financial Service disburses → Customer notified
- [ ] Card Service: Payment processed → Transaction recorded → Notification sent
- [ ] Audit Service: All events logged with proper metadata

---

## 📦 DOCKER COMPOSE UPDATES

Ensure all services are in `docker-compose.yml`:
```yaml
services:
  eureka-server: ✓
  api-gateway: ✓
  rabbitmq: ✓ (Add if missing)
  postgres: ✓ (For IAM, Account, Transaction, Card)
  mongodb: ✓ (For Financial, Notification)
  cassandra: ? (For Audit if using Cassandra)
  
  # Microservices
  iam-service: ✓
  account-service: ✓
  transaction-service: ✓
  financial-service: ✓
  card-service: ✓
  notification-service: ✓
  audit-service: ✓
```

---

## 🚨 CRITICAL DEPENDENCIES

### Maven/POM Updates:
1. **All services need**:
   - Spring Cloud OpenFeign (for inter-service calls)
   - Spring AMQP (RabbitMQ)
   - Lombok
   - Veld generated code dependency

2. **Account Service specifically needs**:
   - Add Veld dependency (missing)
   - Add RabbitMQ
   - Add OpenFeign

3. **Financial Service needs**:
   - Ensure MongoDB driver
   - Add OpenFeign

4. **Notification Service needs**:
   - Spring Mail
   - MongoDB driver
   - RabbitMQ

---

## ✅ DEFINITION OF DONE

Each service is considered DONE when:
1. ✅ VELD definition complete and generates code
2. ✅ All entities/models implemented
3. ✅ All repositories implemented
4. ✅ All service methods implemented (no nulls)
5. ✅ Event publishers configured
6. ✅ Event consumers configured
7. ✅ Feign clients for external service calls
8. ✅ Unit tests written
9. ✅ Integration test passes
10. ✅ Docker image builds
11. ✅ Service registers with Eureka
12. ✅ Postman collection updated

---

## 🎓 RUBRIC ALIGNMENT

### Required Features (from your guidelines):
- ✅ At least 4 functional modules → **We have 7 services**
- ✅ Spring Cloud (Eureka, Gateway) → **Implemented**
- ✅ Event-driven with message broker → **RabbitMQ throughout**
- ✅ Multiple databases → **PostgreSQL + MongoDB + (Cassandra)**
- ✅ RESTful APIs → **All Veld-generated**
- ✅ Docker containerization → **docker-compose.yml exists**
- ✅ Microservices communication → **Feign + RabbitMQ**
- ✅ Role-based access → **IAM service with Admin/Employee/Customer**

### Original 20 Brainstorm Points Mapped:
1. Loan (#1) → Financial Service ✓
2. Withdrawal (#2) → Transaction Service ✓
3. Payment Gateway (#3) → Card Service ✓
4. Deposit (#4) → Transaction Service ✓
5. Transfer (#5) → Transaction Service ✓
6. Account Management (#6) → Account Service (TO BE FIXED)
7. Customer Service (#7) → Account Service ✓
8. Certificates (#8) → Financial Service ✓
9. Investments (#9) → Financial Service (TO BE IMPLEMENTED)
10. Credit Cards (#10) → Card Service ✓
11. Accounts (#11) → Account Service (TO BE FIXED)
12. Debit Cards (#12) → Card Service ✓
13. Transaction (#13) → Transaction Service ✓
14. Log (#14) → Audit Service ✓
15. Workflow/Approval (#15) → Notification Service (TO BE IMPLEMENTED)
16. Currency Exchange (#16) → Financial Service (FX module) ✓
17. Mortgage (#17) → Financial Service (TO BE IMPLEMENTED)
18. Notification Service (#18) → Notification Service (TO BE IMPLEMENTED)
19. Auth Service (#19) → IAM Service ✓

---

## 🚀 NEXT STEPS

**IMMEDIATE ACTION**: Start with Account Service rebuild (Phase 1)
**ESTIMATED TOTAL TIME**: 80-100 hours (2-2.5 weeks full-time)
**TEAM RECOMMENDATION**: 
- Person 1: Account Service
- Person 2: Notification Service
- Person 3: Workflow Service  
- Person 4: Financial Service (Mortgage + Investment)
- Person 5: FX Integration + Testing
- Person 6: Audit Service + Documentation
- Person 7: Frontend integration

**STATUS AFTER COMPLETION**: Production-ready, fully event-driven microservices bank system meeting ALL rubric requirements.

---

**Generated**: 2026-04-21  
**Document Status**: Complete Implementation Blueprint  
**Ready for**: Immediate Development Start

