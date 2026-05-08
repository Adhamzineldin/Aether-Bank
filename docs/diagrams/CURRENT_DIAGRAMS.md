# Current Diagrams Index (auto-generated 2026-04-29)

These `.current.puml` files were generated **from the actual code** and sit
beside the original plan diagrams in `docs/diagrams/` without modifying them.
The originals stay as the historical "plan"; these are the truth.

> Format: PlantUML (same as the originals). Render with `plantuml` or any
> PlantUML viewer.

## OCL Domain Diagrams (Phase 1–2, added 2026-05-08)

These diagrams support the formal OCL specification in `docs/ocl/`.

| File | Purpose |
|---|---|
| `domain-banking-model.puml` | Class/domain model used as the OCL context. Shows `BankAccount`, `Transaction`, `LedgerBalance`, enums, DTOs, and services. OCL rule annotations appear in the notes on each class. |
| `constraint-overview.puml` | Deployment-style map showing how each OCL rule group (`BA_*`, `TX_*`, `LB_*`, `UI_*`) traces to backend and frontend enforcement points. |

---

## Architecture

| New file | Counterpart of | Δ vs the plan |
|---|---|---|
| `architecture/01_microservices_architecture.current.puml` | `01_microservices_architecture.puml` | No `ledger-service` / `workflow-service` / `lending-service` / `Config Server` / `Kafka` / `Forex API` / `Email-SendGrid` / `SMS-Twilio`. Real services: **iam, account, transaction, card, financial, notification, audit**. Messaging is **RabbitMQ** (not Kafka). Notifications go through **SMTP (Gmail)**. |
| `architecture/02_docker_compose_architecture.current.puml` | `02_docker_compose_architecture.puml` | One Postgres container hosting `iam_db`, `account_db`, `transaction_db`, `card_db` (host port **5433**). One MongoDB container hosting `financial_db`, `notification_db`, `audit_db` (host port **27018**). Real container ports: api-gateway **9000**, eureka **8860**, iam **8085**, account **8081**, transaction **8082**, financial **8083**, card **8084**, notification **8086→3030**, audit **8087**, frontend **3000→80**. Adds RabbitMQ (5672/15672) and mongo-express (8091). No MySQL, no Zookeeper, no Kafka. |

## Databases (per service)

| New file | Counterpart of | Δ vs the plan |
|---|---|---|
| `databases/01_db_iam_service.current.puml` | `01_db_iam_service.puml` | Adds `permissions` and `role_permissions` (RBAC). `users` has full MFA + lockout columns (`failed_login_attempts`, `locked_until`, `mfa_enabled`, `mfa_secret`). `audit_logs` is also persisted in iam_db (separate from audit-service `security_logs`). No `login_attempts` table — that data is denormalised into `users`. |
| `databases/02_db_account_service.current.puml` | `02_db_account_service.puml` | PostgreSQL (not MySQL). Two tables: `bank_accounts` (UUID, multi-currency aware) and a **legacy** `customers` table. **No balance column** — balances live in transaction_db. |
| `databases/03_db_transaction_service.current.puml` | `03_db_ledger_service.puml` | Replaces "ledger-service". Tables: `transactions`, `ledger_balance` (composite PK `account_id+currency` for multi-currency wallets), `outbox_messages` (transactional outbox → RabbitMQ). |
| `databases/04_db_card_service.current.puml` | `05_db_card_service.puml` | PostgreSQL (not MySQL). Tables: `cards` (with demo `pan` + `cvv` for the UI), `credit_card_details` (1:1 with credit cards), `card_transactions`. |
| `databases/05_db_financial_service.current.puml` | `04_db_lending_service.puml` | "lending-service" became `financial-service`. **MongoDB** (not just lending). Collections: `loan_product_definitions`, `loan_applications`, `loan_accounts`, `certificate_product_definitions`, `certificate_applications`, `certificate_accounts`, `mortgage_applications`, `investment_accounts`, `asset_holdings`, `ledger_entries`. |
| `databases/06_db_notification_service.current.puml` | `06_db_workflow_service.puml` | "workflow-service" was merged into notification-service. One MongoDB DB owns BOTH notifications (`notification_templates`, `notification_items`) AND the workflow engine (`workflow_templates`, `workflow_instances`, `approval_tasks`). |
| `databases/07_db_audit_service.current.puml` | _(new — no plan counterpart)_ | Single Mongo collection `security_logs`. All fields are free-form strings on purpose so any service can publish new action verbs without an SDK regen. |

## Sequences

| New file | Counterpart of | Δ vs the plan |
|---|---|---|
| `sequences/01_sequence_user_login.current.puml` | `01_sequence_user_login.puml` | JWT (HS256, shared secret with gateway). Lockout after 5 failed attempts. MFA branch (otp_codes table). RabbitMQ-based audit trail. |
| `sequences/02_sequence_money_transfer.current.puml` | `02_sequence_money_transfer.puml` | RabbitMQ (not Kafka). Saga pattern: gateway 202s immediately, `LedgerCommandListener` consumes from `saga.commands.queue`, ledger update + transactional outbox. Notifications + audit fan out from RabbitMQ. |
| `sequences/03_sequence_loan_application.current.puml` | `03_sequence_loan_application.puml` | financial-service owns the application doc; notification-service runs the **workflow engine** (workflow_instances + approval_tasks) and emits `loan.approved.queue`. financial-service then calls transaction-service via TransactionGateway to disburse. |
| `sequences/04_sequence_card_payment.current.puml` | `04_sequence_card_payment.puml` | Adds the embedded **packages/payment-gateway** React component as the merchant-facing surface. Branches credit (in-card_db) vs debit (calls transaction-service). |
| `sequences/05_sequence_service_discovery.current.puml` | `05_sequence_service_discovery.puml` | Spring Cloud Gateway **MVC** (servlet, not reactive). JwtAuthFilter strips spoofed `X-User-Id` / `X-User-Roles` and re-injects them from verified claims. Real route table included. |
| `sequences/06_sequence_account_opening.current.puml` | _(new)_ | Captures wallet provisioning: `AccountCreatedEvent` → RabbitMQ → transaction-service `AccountEventListener` inserts `ledger_balance` row. |
| `sequences/07_sequence_card_issuance.current.puml` | _(new)_ | CardIssueController flow: credit cards self-contained in card_db; debit cards validate the linked account via Veld AccountClient. |

## Use Cases

| New file | Counterpart of | Δ vs the plan |
|---|---|---|
| `usecases/01_usecase_system_overview.current.puml` | `01_usecase_system_overview.puml` | Reflects actual frontend features (`accounts, beneficiaries, cards, fx, investments, loans, mortgages, notifications, payments, profile, savings, transactions, workflow, admin`) and backend controllers. Adds **SuperAdmin** actor (UserManagementController gates mutation to SUPERADMIN). Adds System Scheduler (outbox dispatcher / accrual). Drops "Withdraw / Deposit" — only transfers and card payments are wired. Drops dedicated "Currency Exchange" use case (FX is a query API, not a settlement). Per-service usecase files from the plan (`02-07_usecase_*.puml`) are not regenerated — the overview supersedes them; create individually only if needed for a deliverable. |

## Notable plan-vs-reality discrepancies

1. **Renamed services** — `ledger-service` → `transaction-service`; `lending-service` → `financial-service` (broader scope: loans + mortgages + CDs + investments).
2. **Removed services** — no `config-server`, no `workflow-service` (workflow lives inside notification-service).
3. **Different message broker** — RabbitMQ is used everywhere; Kafka and Zookeeper from the plan are not deployed.
4. **One DB cluster per technology** — a single Postgres instance hosts four logical DBs (created by `init-databases.sql`); a single MongoDB instance hosts three logical DBs. The plan implied separate per-service containers.
5. **No MySQL** — all relational data is PostgreSQL.
6. **No external Forex/SendGrid/Twilio integrations** — only SMTP via Gmail for emails, and an FX query controller in financial-service.
7. **Embedded payment gateway** — `packages/payment-gateway` is a React component library (not a separate backend), and is the actual merchant surface used by the frontend. The plan's "External Merchant → API Gateway with API Key" path is not implemented; merchants funnel through the same JWT-protected `/api/card/**` route via the embedded gateway.
8. **JWT verification at gateway** — all routes except `/api/auth/**` go through `JwtAuthFilter`, which also strips client-supplied `X-User-Id` headers to prevent spoofing and re-injects the verified subject + roles for downstream services.
9. **Transactional outbox** — the transfer flow is asynchronous (HTTP 202) and uses an outbox table + RabbitMQ to guarantee delivery; the plan diagram showed a synchronous request/response.
10. **Multi-currency wallets** — `ledger_balance` is keyed by `(account_id, currency)`, not `account_id` alone. Cross-currency transfers are first-class (`source_currency`, `destination_currency`, `exchange_rate` columns on `transactions`).

