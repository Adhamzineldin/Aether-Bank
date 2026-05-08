# OCL Presentation Notes

Use these notes for a demo, report section, or viva-style explanation.

## 1. Problem statement

The banking platform contains important business rules that are only partially visible in UML diagrams and code annotations.

Examples:

- a transfer amount must be positive
- source and destination accounts must differ
- a closed account must have a closing date
- a pending account may only transition to active
- a ledger balance must not become negative

Without a formal specification language, these rules are scattered across service code, DTO annotations, and frontend validation.

## 2. Why OCL was added

OCL was introduced to provide a **formal, technology-independent specification** for these business constraints.

This helps the project by:

- making business rules precise
- linking UML/domain modeling to implementation
- improving traceability for academic assessment
- clarifying which rules are invariant rules vs operation contracts

## 3. What OCL does in this project

In this repository, OCL is used as a **documentation-first specification layer**.

That means:

- the OCL rules are written explicitly in `docs/ocl/banking-domain.ocl`
- the underlying model is shown in `docs/diagrams/domain-banking-model.puml`
- the rules are traced to real code in `docs/ocl/traceability-matrix.md`
- the runtime system still enforces them through Java, Spring validation, and frontend checks

## 4. Key modeled classes

The first OCL phase focuses on three core entities:

- `BankAccount`
- `Transaction`
- `LedgerBalance`

These were selected because they already contain important business constraints in the codebase.

## 5. Key OCL examples to explain

### Bank account rules
- closed accounts must have `closedDate`
- non-closed accounts should not have `closedDate`
- currency should look like a 3-letter ISO code
- pending accounts may only transition to active

### Transaction rules
- transfer amount must be positive
- source and destination accounts must be different
- FX transfers must have source currency, destination currency, destination amount, and exchange rate

### Ledger rules
- available balance must never be negative
- ledger identity must include both account id and currency

## 6. How this maps to the implementation

### Backend
- `BankAccountService` enforces account lifecycle rules
- `TransactionValidator` enforces transfer rules
- `LedgerBalance.debit()` enforces non-negative balance behavior

### Frontend
- `TransferPage.tsx` mirrors some rules using a Zod form schema

### DTO validation
- `OpenAccountRequest` applies structural checks such as `customerId`, `accountType`, and currency pattern

## 7. Important design decision

The project does **not** currently execute OCL directly inside the Spring Boot runtime.

Instead, it uses the following split:

- **OCL** = formal source of truth for model constraints
- **Java/React code** = executable enforcement of those constraints

This is a practical choice because the existing architecture is code-first and microservice-based.

## 8. What Phase 1 delivered

Phase 1 delivered:

- an initial banking domain diagram
- an initial OCL specification file
- rule naming conventions
- a focused domain scope for accounts, transactions, and ledger balances

## 9. What Phase 2 delivered

Phase 2 delivered:

- traceability between formal rules and code
- a constraint overview diagram
- presentation/report guidance
- a clear explanation of where the rules are enforced today

## 10. Suggested talking points for assessment

You can say:

> We used OCL to formally specify the core banking constraints of the system. Although our microservice implementation is Java/Spring and React-based, the OCL layer gives us a model-level definition of correctness. We then mapped those rules to DTO validation, service-level logic, entity behavior, and frontend checks. This improves traceability and makes the design more rigorous.

## 11. Suggested future extension points

If more time is available, the next steps would be:

- add OCL rules for cards and lending
- label automated tests with OCL rule IDs
- harden code for currently conceptual rules like `pendingHolds >= 0`
- optionally prototype Eclipse OCL evaluation as a separate modeling artifact

