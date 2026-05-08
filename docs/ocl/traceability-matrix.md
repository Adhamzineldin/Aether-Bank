# OCL Traceability Matrix

This matrix links each OCL rule to its current implementation point in the repository.

## Reading the matrix

- **OCL Rule**: identifier used in `docs/ocl/banking-domain.ocl`
- **Intent**: what the rule means in business terms
- **Primary implementation**: where the rule is currently enforced
- **Layer**: model, DTO, service, entity, or UI
- **Notes**: gaps, caveats, or presentation comments

| OCL Rule | Intent | Primary implementation | Layer | Notes |
|---|---|---|---|---|
| `BA_01_ClosedAccountsHaveClosedDate` | closed account must record closing date | `backend/account-service/src/main/java/com/maayn/accountservice/service/BankAccountService.java` | service/entity | `closeAccount()` sets `closedDate` when status becomes `CLOSED` |
| `BA_02_OnlyClosedAccountsMayHaveClosedDate` | only closed accounts should carry `closedDate` | conceptual rule, implied by service flow | model/service | useful specification rule even if not separately coded yet |
| `BA_03_OpenedDateRequired` | every bank account must have an opening date | `BankAccountService.openAccount()` | service/entity | `openedDate` is set to `LocalDate.now()` |
| `BA_04_CurrencyLooksISO3` | persisted account currency should look like `USD`, `EUR` | `backend/account-service/src/main/java/com/maayn/accountservice/dto/OpenAccountRequest.java` | DTO | enforced before account creation with `@Pattern` |
| `BA_05_AccountNumberRequired` | account must have an account number | `BankAccountService.openAccount()` and `BankAccount.java` | service/entity | generated through `AccountNumberGenerator` |
| `BA_06_AccountMustNotAlreadyBeClosed` | cannot close an account twice | `BankAccountService.closeAccount()` | service | throws `InvalidAccountStatusException` |
| `BA_07_PositiveBalanceRequiresTransferTarget` | closing account with balance needs transfer destination | `BankAccountService.closeAccount()` | service | enforced when balance is greater than zero |
| `BA_08_StatusBecomesClosed` | after close operation, account becomes closed | `BankAccountService.closeAccount()` | service | account status explicitly set to `CLOSED` |
| `BA_09_ClosedDateAssigned` | after close operation, close date is recorded | `BankAccountService.closeAccount()` | service | date explicitly assigned |
| `BA_10_ClosedAccountsCannotChangeStatus` | closed accounts are immutable with respect to status | `BankAccountService.validateStatusTransition()` | service | direct rule match |
| `BA_11_StatusMayNotBeSetDirectlyToClosed` | status update endpoint may not directly close account | `BankAccountService.validateStatusTransition()` | service | closure must use `closeAccount()` |
| `BA_12_PendingMayOnlyBecomeActive` | pending accounts can only transition to active | `BankAccountService.validateStatusTransition()` | service | direct rule match |
| `BA_13_CustomerIdRequired` | account open request requires customer id | `OpenAccountRequest.java` | DTO | enforced with `@NotNull` |
| `BA_14_AccountTypeRequired` | account open request requires account type | `OpenAccountRequest.java` | DTO | enforced with `@NotNull` |
| `BA_15_RequestCurrencyLooksISO3` | request currency format must be 3 uppercase letters | `OpenAccountRequest.java` | DTO | enforced with `@Pattern` |
| `TX_01_PositiveAmount` | transfer amount must be greater than zero | `backend/transaction-service/src/main/java/com/maayn/transactionservice/validators/TransactionValidator.java` | validator | enforced by `validatePositiveAmount()` |
| `TX_02_DifferentSourceAndDestination` | source and destination must differ | `TransactionValidator.java` | validator | enforced by `validateDifferentAccounts()` |
| `TX_03_CurrencyRequired` | transaction currency must be present | `Transaction.java` and mapping flow | entity/service | explicit non-null JPA column plus mapper/service assumptions |
| `TX_04_ReferenceNumberRequired` | transaction must have reference number | `Transaction.java` | entity | persisted field marked `nullable = false` |
| `TX_05_IdempotencyKeyRequired` | transaction must have idempotency key | `Transaction.java`, `TransactionService.transfer()` | entity/service | used to prevent duplicate processing |
| `TX_06_FxTransfersNeedSourceCurrency` | FX transfer requires source currency | `TransactionService.prepareTransaction()` | service | source/destination currencies used for rate lookup |
| `TX_07_FxTransfersNeedDestinationCurrency` | FX transfer requires destination currency | `TransactionService.prepareTransaction()` | service | destination currency used for conversion |
| `TX_08_FxTransfersNeedDestinationAmount` | FX transfer requires calculated destination amount | `TransactionService.prepareTransaction()` | service | computed from amount and exchange rate |
| `TX_09_FxTransfersNeedExchangeRate` | FX transfer requires positive rate | `TransactionService.prepareTransaction()` and `FxRateService` | service | derived before persistence |
| `LB_01_AvailableBalanceNonNegative` | balance may not become negative | `backend/transaction-service/src/main/java/com/maayn/transactionservice/entity/LedgerBalance.java` | entity | `debit()` throws on insufficient funds |
| `LB_02_PendingHoldsNonNegative` | holds should remain non-negative | conceptual rule | model/entity | not yet separately enforced in code |
| `LB_03_CurrencyRequired` | ledger identity requires currency | `LedgerBalance.java`, `LedgerService.java` | entity/service | created with `LedgerAccountId(accountId, currency)` |
| `LB_04_AccountIdRequired` | ledger identity requires account id | `LedgerBalance.java`, `LedgerService.java` | entity/service | required to create and fetch balances |
| `UI_01_SourceAccountRequired` | transfer form requires source account | `frontend/src/features/transactions/pages/TransferPage.tsx` | UI | zod schema uses UUID string |
| `UI_02_DestinationRequired` | transfer form requires account/IBAN/UUID input | `TransferPage.tsx` | UI | zod minimum length and resolution step |
| `UI_03_AmountMustBePositive` | transfer form blocks zero/negative amount | `TransferPage.tsx` | UI | zod `refine((v) => Number(v) > 0)` |

## Interpretation guidance

### Authoritative rule source
The OCL file is the formal source of truth for the documented business constraints.

### Runtime enforcement
The runtime system currently enforces those rules through:

- Bean Validation annotations (`@NotNull`, `@Pattern`, `@Positive`)
- service-level business logic
- entity/domain methods
- frontend Zod validation

### Known gaps
Some rules are currently stronger in the OCL specification than in executable code. That is acceptable as long as it is presented honestly as:

1. a formal model requirement, and
2. a target for future hardening.

Recommended future hardening candidates:

- explicit prevention of stray `closedDate` values on non-closed accounts
- explicit enforcement that `pendingHolds >= 0`
- direct test coverage named after OCL rule IDs

