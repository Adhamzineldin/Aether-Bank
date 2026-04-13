# Transaction Service

Owns the **ledger**, **transfers**, and **transaction history** for Aether Bank.  
No other service touches balances directly — everything goes through here.

---

## Table of Contents

1. [What This Service Does](#1-what-this-service-does)
2. [Internal Architecture](#2-internal-architecture)
3. [RabbitMQ — What to Send (Inputs)](#3-rabbitmq--what-to-send-inputs)
4. [RabbitMQ — What to Listen For (Outputs)](#4-rabbitmq--what-to-listen-for-outputs)
5. [REST API](#5-rest-api)
6. [Business Rules & Validation](#6-business-rules--validation)
7. [Idempotency](#7-idempotency)
8. [Outbox Pattern](#8-outbox-pattern)
9. [Error Reference](#9-error-reference)
10. [Integration Checklist per Service](#10-integration-checklist-per-service)

---

## 1. What This Service Does

| Responsibility | Details |
|---|---|
| **Ledger management** | Maintains a `ledger_balance` record for every account. Balances are the ground truth. |
| **Transfer execution** | Debits source, credits destination atomically. Rejects invalid/insufficient transfers. |
| **Transaction history** | Stores every transaction record with status, reference number, timestamps. |
| **Event publishing** | After every transfer attempt (success or failure), publishes an event to RabbitMQ via the Outbox pattern so downstream services (Notification, etc.) react reliably. |

---

## 2. Internal Architecture

```
Incoming RabbitMQ Message
        │
        ▼
┌──────────────────────┐     ┌────────────────────────┐
│ AccountEventListener │     │ LedgerCommandListener  │
│ (ledger.account.     │     │ (ledger.commands.queue)│
│  events.queue)       │     └────────────┬───────────┘
└────────┬─────────────┘                  │
         │ Creates LedgerBalance          │ TransferRequest
         ▼                                ▼
  LedgerBalanceRepository        TransactionService
                                          │
                              ┌───────────┼────────────┐
                              ▼           ▼            ▼
                     Idempotency    Validator     LedgerService
                     Handler        (amount > 0,  (debit/credit)
                     (deduplicate)   ≠ same acct)
                              │
                              ▼
                     TransactionRepository (save)
                              │
                              ▼
                     TransactionEventPublisher
                              │
                              ▼
                       OutboxMessage (DB)
                              │
                    ┌─────────┘ (every 5s)
                    ▼
             OutboxRelaySweeper ──► RabbitMQ (banking.exchange)
```

**Key design decisions:**
- `LedgerService.executeTransferMath()` runs with `Propagation.MANDATORY` — it will never start its own transaction. It must be called from within an existing one.
- The Outbox table acts as a buffer: the transfer is committed to DB and the event is written atomically. RabbitMQ delivery happens separately in the sweeper job. This means **RabbitMQ being down cannot cause a lost event**.
- `@Version` on both `Transaction` and `LedgerBalance` provides optimistic locking against concurrent modification.

---

## 3. RabbitMQ — What to Send (Inputs)

### 3.1 — Account Service: Publish `AccountCreatedEvent`

Whenever a new account is created, the Account Service **must** publish this event so the Transaction Service can initialize a ledger balance for it. Without this, any future transfer to/from that account will be rejected.

**Exchange:** `banking.exchange`  
**Queue (consumed by Transaction Service):** `ledger.account.events.queue`  
**Routing key:** _(bind to this queue from your exchange — Transaction Service owns the queue declaration)_

**Message payload:**
```json
{
  "accountId": "uuid",
  "currency": "USD",
  "createdAt": "2026-04-11T10:00:00"
}
```

| Field | Type | Required | Notes |
|---|---|---|---|
| `accountId` | UUID | ✅ | The new account's ID. Used as the ledger primary key. |
| `currency` | String | ✅ | e.g. `"USD"`, `"EUR"` |
| `createdAt` | LocalDateTime | ✅ | ISO-8601 format |

**What happens on receipt:**
- Transaction Service checks if a ledger balance already exists for that `accountId`.
- If not, creates one with `availableBalance = 0.00`.
- If it already exists, the event is silently ignored (idempotent).

---

### 3.2 — Card Service / Financial Service: Publish `TransferRequest`

To execute a transfer (card payment, SAGA step, peer transfer, etc.), publish a `TransferRequest` to the SAGA commands queue.

**Exchange:** `banking.exchange`  
**Queue (consumed by Transaction Service):** `ledger.commands.queue`

**Message payload:**
```json
{
  "idempotencyKey": "card-pay-001",
  "sourceAccountId": "uuid",
  "destinationAccountId": "uuid",
  "amount": "150.00",
  "currency": "USD",
  "type": "CARD_PAYMENT"
}
```

| Field | Type | Required | Notes |
|---|---|---|---|
| `idempotencyKey` | String | ✅ | **Must be unique per business operation.** Duplicate keys return the original result safely. |
| `sourceAccountId` | UUID | ✅ | The account being debited. Must have an initialized ledger. |
| `destinationAccountId` | UUID | ✅ | The account being credited. Must be different from source. Must have an initialized ledger. |
| `amount` | BigDecimal | ✅ | Must be `> 0`. |
| `currency` | String | ✅ | e.g. `"USD"` |
| `type` | TransactionType | ✅ | See valid values below. |

**Valid `type` values:**

| Value | Use case |
|---|---|
| `TRANSFER` | Generic peer-to-peer transfer |
| `INTERNAL_TRANSFER` | Internal bank movement |
| `DEPOSIT` | Crediting an account from external source |
| `WITHDRAWAL` | Debiting an account to external destination |
| `CARD_PAYMENT` | Card Service initiated payment |
| `BILL_PAYMENT` | Bill payment deduction |

**What happens on receipt:**
1. Idempotency check — if `idempotencyKey` already exists, the original result is returned, no double-processing.
2. Validation — amount > 0, source ≠ destination.
3. Ledger debit/credit — atomically within a DB transaction.
4. Transaction saved with `status = SUCCESS`.
5. `TransferSuccessEvent` written to Outbox → relayed to `banking.exchange` with routing key `transaction.transfer.success`.

**On any failure:**
- Transaction is NOT persisted.
- `TransferFailedEvent` is written to Outbox → relayed to `banking.exchange` with routing key `transaction.transfer.failed`.

---

## 4. RabbitMQ — What to Listen For (Outputs)

Both events are published to the same exchange. Bind your queues to the routing keys you care about.

**Exchange:** `banking.exchange`

---

### 4.1 `TransferSuccessEvent`

**Routing key:** `transaction.transfer.success`

```json
{
  "referenceNumber": "TXN-A1B2C3D4",
  "sourceAccountId": "uuid",
  "destinationAccountId": "uuid",
  "amount": "150.00",
  "currency": "USD",
  "eventTime": "2026-04-11T10:05:00"
}
```

| Field | Type | Notes |
|---|---|---|
| `referenceNumber` | String | Unique reference for this transaction. Format: `TXN-XXXXXXXX` |
| `sourceAccountId` | UUID | Account that was debited |
| `destinationAccountId` | UUID | Account that was credited |
| `amount` | BigDecimal | Amount transferred |
| `currency` | String | Currency code |
| `eventTime` | LocalDateTime | When the transaction completed |

**Who should consume this:**
- **Notification Service** → send transfer confirmation to user
- **Audit Service** → log the successful transaction
- **Card Service** → mark card payment as settled

---

### 4.2 `TransferFailedEvent`

**Routing key:** `transaction.transfer.failed`

```json
{
  "referenceNumber": "card-pay-001",
  "sourceAccountId": "uuid",
  "destinationAccountId": "uuid",
  "amount": "150.00",
  "currency": "USD",
  "eventTime": "2026-04-11T10:05:01",
  "failureReason": "Insufficient funds in account: <uuid>"
}
```

| Field | Type | Notes |
|---|---|---|
| `referenceNumber` | String | The `idempotencyKey` from the original `TransferRequest` |
| `failureReason` | String | Human-readable reason for failure |
| _(rest same as success)_ | | |

**Who should consume this:**
- **Notification Service** → send decline/failure alert to user
- **Card Service** → mark card payment as declined, reverse any holds
- **Lending Service** → handle failed repayment

---

## 5. REST API

Base path: `/api/transaction_service`

> **Note:** All REST endpoints are also reachable through the API Gateway.

---

### `POST /transactions/transfer`

Execute a transfer directly over HTTP (bypasses RabbitMQ). Useful for synchronous flows where the caller needs an immediate response.

**Request body:** Same as `TransferRequest` above.

**Response `201 Created`:**
```json
{
  "referenceNumber": "TXN-A1B2C3D4",
  "amount": "150.00",
  "status": "SUCCESS",
  "timestamp": "2026-04-11T10:05:00"
}
```

**Error response:**
```json
{
  "error": "INSUFFICIENT_FUNDS",
  "message": "Insufficient funds in account: <uuid>",
  "status": 400
}
```

---

### `GET /transactions/account/{accountId}`

Fetch paginated transaction history for an account. Returns all transactions where the account was either source or destination, sorted newest-first.

**Request body (pagination):**
```json
{
  "page": 0,
  "pageSize": 20
}
```

**Response `200 OK`:**
```json
{
  "content": [
    {
      "referenceNumber": "TXN-A1B2C3D4",
      "amount": "150.00",
      "status": "SUCCESS",
      "timestamp": "2026-04-11T10:05:00"
    }
  ],
  "page": 0,
  "pageSize": 20,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

---

### `GET /ledger/{accountId}/balance`

Get the current real-time ledger balance for an account.

**Response `200 OK`:**
```json
{
  "accountId": "uuid",
  "availableBalance": "850.00",
  "currency": "USD"
}
```

**Error (account ledger not initialized):** `404` with error body.

---

## 6. Business Rules & Validation

These rules are enforced on **every** transfer regardless of channel (REST or RabbitMQ):

| Rule | Error thrown |
|---|---|
| `amount` must be `> 0` | `INVALID_AMOUNT` |
| `sourceAccountId` ≠ `destinationAccountId` | `INVALID_TARGET` |
| Both accounts must have an initialized ledger | `INVALID_TARGET` — "Transfer failed: Account Ledger not initialized." |
| Source account must have sufficient `availableBalance` | `INSUFFICIENT_FUNDS` |

**Debit/credit is atomic** — both balance updates happen in the same DB transaction. If the credit fails, the debit is rolled back.

---

## 7. Idempotency

Every `TransferRequest` must include a unique `idempotencyKey`.

**Behavior:**
- First time a key is seen → transfer is processed normally.
- Subsequent requests with the same key → the original `TransactionResponse` is returned immediately, **no second debit occurs**.
- This is safe to retry on network failures.

**Your responsibility:** Generate a stable, unique key per business operation. Good examples:
```
card-payment-{cardId}-{merchantRef}
saga-{sagaId}-step-{stepNumber}
bill-pay-{billId}-{cycleDate}
```

Bad examples (do not use): random UUIDs generated at call time, timestamps alone.

---

## 8. Outbox Pattern

The Transaction Service never publishes directly to RabbitMQ during a transfer. Instead:

1. The event (`TransferSuccessEvent` or `TransferFailedEvent`) is **serialized and saved to the `outbox_messages` table** within the same DB transaction as the transfer itself.
2. The `OutboxRelaySweeper` job runs every **5 seconds**, reads all pending outbox rows in order, and publishes them to RabbitMQ.
3. Once published successfully, the row is deleted.
4. If RabbitMQ is down, the sweeper logs a warning and halts the batch — it will retry the same messages on the next cycle. **No messages are lost.**

**Implication for consumers:** There may be up to ~5 seconds of delay between a transfer completing and the event arriving on your queue. Design your consumers to be tolerant of this.

---

## 9. Error Reference

| Error key | HTTP Status | Meaning |
|---|---|---|
| `INSUFFICIENT_FUNDS` | 400 | Source account balance is too low |
| `INVALID_AMOUNT` | 400 | Amount is zero or negative |
| `INVALID_TARGET` | 400 | Same source/destination, or ledger not initialized |
| `ACCOUNT_LEDGER_NOT_INITIALIZED` | 404 | Tried to get balance for an account with no ledger record |

---

## 10. Integration Checklist per Service

### Account Service
- [ ] Publish `AccountCreatedEvent` to `banking.exchange` bound to `ledger.account.events.queue` after every new account is persisted.
- [ ] Ensure `accountId`, `currency`, and `createdAt` are always populated.

### Card Service
- [ ] Publish `TransferRequest` to `ledger.commands.queue` for every card payment attempt.
- [ ] Use a stable `idempotencyKey` (e.g. `card-{transactionRef}`).
- [ ] Listen on `transaction.transfer.success` → mark payment as settled.
- [ ] Listen on `transaction.transfer.failed` → reverse any holds, mark payment as declined.

### Financial / Lending Service
- [ ] Publish `TransferRequest` to `ledger.commands.queue` for loan disbursements and repayments.
- [ ] Use a stable `idempotencyKey` (e.g. `loan-{loanId}-disbursement`, `loan-{loanId}-repayment-{cycleId}`).
- [ ] Listen on `transaction.transfer.failed` to handle failed repayment scenarios.

### Notification Service
- [ ] Bind a queue to `transaction.transfer.success` on `banking.exchange`.
- [ ] Bind a queue to `transaction.transfer.failed` on `banking.exchange`.
- [ ] Use `referenceNumber` / `sourceAccountId` to look up user contact details and send alerts.

### Audit Service
- [ ] Bind a queue to both routing keys and log all transfer outcomes.

