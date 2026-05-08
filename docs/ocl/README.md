# OCL Specification Package

This folder introduces **Object Constraint Language (OCL)** as a formal specification layer for the Aether Bank domain model.

## Goal

The current project already enforces many business rules in code, especially in:

- `backend/account-service/src/main/java/com/maayn/accountservice/service/BankAccountService.java`
- `backend/account-service/src/main/java/com/maayn/accountservice/dto/OpenAccountRequest.java`
- `backend/transaction-service/src/main/java/com/maayn/transactionservice/validators/TransactionValidator.java`
- `backend/transaction-service/src/main/java/com/maayn/transactionservice/entity/LedgerBalance.java`
- `frontend/src/features/transactions/pages/TransferPage.tsx`

This OCL package makes those rules **formal, explicit, and traceable**.

Instead of treating validation only as framework-specific annotations or UI checks, the project now documents business constraints at the model level and maps them to the executable implementation.

## Scope

The first implementation phases focus on the core banking model:

- `BankAccount`
- `Transaction`
- `LedgerBalance`
- Account operations:
  - `openAccount`
  - `closeAccount`
  - `updateAccountStatus`
- Transfer flow:
  - request validation
  - service validation
  - ledger consistency

## Phase structure

### Phase 1 — Formal model and constraints
Phase 1 introduces:

- a compact domain class diagram in `docs/diagrams/domain-banking-model.puml`
- a first set of OCL constraints in `docs/ocl/banking-domain.ocl`

### Phase 2 — Traceability and presentation
Phase 2 introduces:

- a traceability matrix in `docs/ocl/traceability-matrix.md`
- a constraint summary diagram in `docs/diagrams/constraint-overview.puml`
- presentation/report notes in `docs/ocl/presentation-notes.md`

## How to read the OCL file

The OCL file uses three main kinds of rules:

- **Invariant (`inv`)**: must always hold for an object
- **Precondition (`pre`)**: must be true before an operation runs
- **Postcondition (`post`)**: must be true after an operation finishes

Example:

```ocl
context Transaction
inv TX_01_PositiveAmount:
    self.amount > 0
```

This means every `Transaction` must have a positive amount.

## Naming convention

Rule IDs are grouped by domain area:

- `BA_*` = bank account rules
- `TX_*` = transaction rules
- `LB_*` = ledger balance rules
- `UI_*` = user-interface mirrored checks

## Important implementation note

OCL in this repository is currently used as a **formal specification and documentation layer**, not as a runtime OCL engine.

That means:

- OCL defines the intended business rule precisely
- Spring validation, service methods, and frontend schemas enforce the rule at runtime
- tests can be mapped back to the OCL rule IDs

This approach fits the current microservice architecture while still satisfying the requirement for explicit OCL usage.

## File map

- `docs/ocl/banking-domain.ocl` — formal OCL constraints
- `docs/ocl/traceability-matrix.md` — OCL rule to implementation mapping
- `docs/ocl/presentation-notes.md` — presentation/report support notes
- `docs/diagrams/domain-banking-model.puml` — domain model used by the OCL rules
- `docs/diagrams/constraint-overview.puml` — high-level overview of where constraints are enforced

## Source model references

The initial model is derived from:

- `backend/account-service/src/main/java/com/maayn/accountservice/entity/BankAccount.java`
- `backend/account-service/src/main/java/com/maayn/accountservice/enums/AccountStatus.java`
- `backend/account-service/src/main/java/com/maayn/accountservice/enums/AccountType.java`
- `backend/transaction-service/src/main/java/com/maayn/transactionservice/entity/Transaction.java`
- `backend/transaction-service/src/main/java/com/maayn/transactionservice/entity/LedgerBalance.java`
- `backend/transaction-service/src/main/java/com/maayn/transactionservice/service/LedgerService.java`
- `backend/transaction-service/src/main/java/com/maayn/transactionservice/service/TransactionService.java`
- `frontend/src/features/transactions/pages/TransferPage.tsx`

