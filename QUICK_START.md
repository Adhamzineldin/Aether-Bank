# QUICK START GUIDE
## Aether Bank Backend - How to Run & Test

---

## 🚀 STARTUP SEQUENCE

### Step 1: Start Infrastructure Services

```powershell
# Start PostgreSQL (if not running)
# Ensure these databases exist:
# - account_db
# - transaction_db
# - iam_db
# - card_db

# Start MongoDB (if not running)
# Ensure it's accessible on localhost:27017

# Start RabbitMQ
# Option 1: Docker
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# Option 2: Local installation
# Access management UI: http://localhost:15672
# Default credentials: guest/guest
```

### Step 2: Start Eureka Server

```powershell
cd "G:\University\Third Year Term 2\SE-2\Aether Bank\backend\eureka-server"
mvn spring-boot:run
```

Wait for Eureka to fully start (check http://localhost:8761)

### Step 3: Start Microservices (in order)

```powershell
# Terminal 1: IAM Service
cd "G:\University\Third Year Term 2\SE-2\Aether Bank\backend\iam-service"
mvn spring-boot:run

# Terminal 2: Account Service
cd "G:\University\Third Year Term 2\SE-2\Aether Bank\backend\account-service"
mvn spring-boot:run

# Terminal 3: Transaction Service
cd "G:\University\Third Year Term 2\SE-2\Aether Bank\backend\transaction-service"
mvn spring-boot:run

# Terminal 4: Financial Service
cd "G:\University\Third Year Term 2\SE-2\Aether Bank\backend\financial-service"
mvn spring-boot:run

# Terminal 5: Card Service
cd "G:\University\Third Year Term 2\SE-2\Aether Bank\backend\card-service"
mvn spring-boot:run

# Terminal 6: Notification Service
cd "G:\University\Third Year Term 2\SE-2\Aether Bank\backend\notification-service"
mvn spring-boot:run

# Terminal 7: Audit Service (optional)
cd "G:\University\Third Year Term 2\SE-2\Aether Bank\backend\audit-service"
mvn spring-boot:run
```

### Step 4: Start API Gateway

```powershell
cd "G:\University\Third Year Term 2\SE-2\Aether Bank\backend\api-gateway"
mvn spring-boot:run
```

---

## 🧪 TEST SCENARIOS

### Scenario 1: Open Bank Account

```bash
# 1. Register a customer (via IAM or Account Service)
POST http://localhost:8081/api/customers/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "1234567890"
}

# Response: Save customerId

# 2. Open a bank account
POST http://localhost:8081/api/accounts
Content-Type: application/json

{
  "customerId": "CUSTOMER_ID_FROM_STEP_1",
  "accountType": "CHECKING",
  "currency": "USD",
  "initialDeposit": 1000.00
}

# Response: Save accountId and accountNumber
```

### Scenario 2: Verify Ledger Initialization

```bash
# Check balance (should return 0 initially, then initialDeposit if implemented)
GET http://localhost:8082/api/transaction_service/ledger/{accountId}/USD/balance
```

### Scenario 3: Transfer Money

```bash
# Assuming you have two accounts: sourceAccountId and destAccountId

POST http://localhost:8082/api/transaction_service/transactions/transfer
Content-Type: application/json

{
  "idempotencyKey": "UNIQUE_KEY_12345",
  "sourceAccountId": "SOURCE_ACCOUNT_ID",
  "destinationAccountId": "DEST_ACCOUNT_ID",
  "amount": 100.50,
  "currency": "USD",
  "sourceCurrency": "USD",
  "destinationCurrency": "USD",
  "type": "TRANSFER"
}

# This will:
# 1. Fetch FX rate from Financial Service
# 2. Execute transfer
# 3. Publish TransferSuccessEvent
# 4. Notification Service receives event
```

### Scenario 4: Submit Loan Application

```bash
POST http://localhost:8083/api/financial_service/api/loan/application/submit
Content-Type: application/json

{
  "customerId": "CUSTOMER_ID",
  "productId": "LOAN_PRODUCT_ID",
  "loanType": "PERSONAL",
  "requestedAmount": 10000.00,
  "requestedTenure": 24,
  "purpose": "Home renovation",
  "employmentStatus": "EMPLOYED",
  "annualIncome": 50000.00
}

# This will:
# 1. Save loan application
# 2. Publish LoanSubmittedEvent
# 3. Notification Service receives event
# 4. Workflow instance created
# 5. First approval task created (RISK role)
```

### Scenario 5: Check Workflow Status

```bash
# Get all workflows
GET http://localhost:8084/api/notification/api/workflow

# Get specific workflow
GET http://localhost:8084/api/notification/api/workflow/{workflowId}

# Get tasks for a workflow
GET http://localhost:8084/api/notification/api/workflow/tasks/{workflowId}
```

### Scenario 6: Approve Loan (Employee Action)

```bash
POST http://localhost:8084/api/notification/api/workflow/tasks/{taskId}/decision
Content-Type: application/json

{
  "employeeId": "EMPLOYEE_ID",
  "decision": "APPROVED",
  "comment": "Application looks good"
}

# This will:
# 1. Mark task as APPROVED
# 2. Create next task (MANAGER role)
# 3. If final approval, publish LoanApprovedEvent
# 4. Financial Service receives event
# 5. Loan disbursement triggered (TODO: needs implementation)
```

### Scenario 7: Get FX Rate

```bash
GET http://localhost:8083/api/financial_service/fx/exchange-rate?sourceCurrency=USD&destinationCurrency=EUR

# Response:
{
  "sourceCurrency": "USD",
  "destinationCurrency": "EUR",
  "exchangeRate": 0.92,
  "timestamp": "2026-04-21T10:30:00"
}
```

---

## 🔍 MONITORING & DEBUGGING

### Check Eureka Dashboard
```
http://localhost:8761
```
All services should be registered and UP.

### Check RabbitMQ Management UI
```
http://localhost:15672
```
- Username: guest
- Password: guest

**Verify:**
- Exchange `bank.events` exists
- Queues created:
  - account.created.queue
  - transaction.success.queue
  - loan.submitted.queue
  - loan.approved.queue

### Check Service Logs

Watch for these log messages:

**Account Service:**
```
Account created successfully: AETH-2026-XXXXXXXXXX
Published AccountCreatedEvent for account: {accountId}
```

**Transaction Service:**
```
Fetched live FX rate from Financial Service: USD -> EUR = 0.92
Transfer {referenceNumber} successful
```

**Notification Service:**
```
Created LOAN workflow template with 3 approval steps
Received loan submission event: {event}
Created workflow instance for loan: {loanId}
```

**Financial Service:**
```
Loan application {id} submitted successfully
Published LoanSubmittedEvent for loan: {loanId}
```

---

## 🐛 COMMON ISSUES & FIXES

### Issue 1: Feign Client "Service Not Found"
**Symptom:** `java.net.UnknownHostException: transaction-service`

**Fix:**
- Ensure Eureka Server is running
- Check service is registered in Eureka dashboard
- Wait 30 seconds after service startup for Eureka registration

### Issue 2: RabbitMQ Connection Refused
**Symptom:** `Connection refused: localhost:5672`

**Fix:**
```powershell
# Check if RabbitMQ is running
docker ps | grep rabbitmq

# Start RabbitMQ if not running
docker start rabbitmq
```

### Issue 3: MongoDB Connection Failed
**Symptom:** `MongoSocketOpenException`

**Fix:**
- Ensure MongoDB is running on localhost:27017
- Check connection string in application.properties

### Issue 4: Lombok Not Working
**Symptom:** `Cannot resolve method 'getId'` or `Cannot resolve symbol 'log'`

**Fix:**
```powershell
# Reimport Maven dependencies
cd backend/account-service
mvn clean install -DskipTests

# In IntelliJ: File → Invalidate Caches → Restart
```

### Issue 5: Events Not Being Consumed
**Symptom:** Loan submitted but no workflow created

**Fix:**
- Check RabbitMQ UI - are messages in queue?
- Check consumer logs for errors
- Verify queue bindings match routing keys

---

## 📊 SERVICE PORTS

| Service | Port | URL |
|---------|------|-----|
| Eureka Server | 8761 | http://localhost:8761 |
| API Gateway | 8080 | http://localhost:8080 |
| IAM Service | 8085 | http://localhost:8085 |
| Account Service | 8081 | http://localhost:8081 |
| Transaction Service | 8082 | http://localhost:8082 |
| Financial Service | 8083 | http://localhost:8083 |
| Card Service | 8084 | http://localhost:8084 |
| Notification Service | 8086 | http://localhost:8086 |
| Audit Service | 8087 | http://localhost:8087 |

---

## 🎯 COMPLETE TEST FLOW

### Full Banking Operation (End-to-End)

1. **Register Customer** → IAM/Account Service
2. **Open Checking Account** → Account Service
   - ✅ AccountCreatedEvent published
   - ✅ Transaction Service initializes ledger
3. **Deposit Funds** → Transaction Service
   - ✅ Balance updated
4. **Open Savings Account** → Account Service
5. **Transfer Money** (Checking → Savings) → Transaction Service
   - ✅ FX rate fetched from Financial Service
   - ✅ Transfer executed
   - ✅ TransferSuccessEvent published
   - ✅ Notification sent (TODO: email)
6. **Apply for Loan** → Financial Service
   - ✅ LoanSubmittedEvent published
   - ✅ Workflow created
   - ✅ Task assigned to RISK analyst
7. **Approve Loan (3 levels)** → Notification Service
   - RISK analyst approves
   - MANAGER approves
   - DIRECTOR approves
   - ✅ LoanApprovedEvent published
8. **Disburse Loan** → Financial Service (TODO)
   - Funds transferred to customer account
   - ✅ LoanDisbursedEvent published
9. **Check Account Balance** → Account Service
   - Shows combined balances

---

## 📋 PRE-DEPLOYMENT CHECKLIST

- [ ] All services start without errors
- [ ] All services registered in Eureka
- [ ] RabbitMQ queues created
- [ ] Database connections working
- [ ] Account creation works
- [ ] Transaction execution works
- [ ] FX rate fetching works
- [ ] Loan submission works
- [ ] Workflow creation works
- [ ] Event flow verified

---

## 🚀 BUILD FOR PRODUCTION

```powershell
# Build all services
cd "G:\University\Third Year Term 2\SE-2\Aether Bank\backend"

# Build each service
foreach ($service in @("account-service", "transaction-service", "financial-service", "notification-service")) {
    cd $service
    mvn clean package -DskipTests
    cd ..
}

# Docker build (if Dockerfiles exist)
docker-compose build

# Docker run
docker-compose up -d
```

---

**Last Updated**: April 21, 2026  
**Version**: 1.0  
**Status**: Ready for Testing

