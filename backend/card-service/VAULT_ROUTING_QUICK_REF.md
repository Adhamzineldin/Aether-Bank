# Quick Reference - Dynamic Merchant Vault Routing

## ✅ COMPLETE

The hardcoded `getMerchantVaultAccount()` method has been replaced with a fully dynamic system.

---

## What Changed

### Hardcoded (Before ❌)
```java
// Hardcoded in CreditCardPaymentFlow
private UUID getMerchantVaultAccount() {
    return"111111111111";  // ❌ Fixed, broken
}

// Hardcoded in DebitCardPaymentFlow
private String getMerchantVaultAccount() {
    return "99999999-9999-9999-9999-999999999998";  // ❌ Always same
}
```

### Dynamic (After ✅)
```java
// In both payment flows
private final MerchantVaultAccountService merchantVaultAccountService;

String vaultAccount = merchantVaultAccountService
    .getVaultAccountForMerchant(merchantId);
```

---

## New Service

**MerchantVaultAccountService**
```
registerMerchantVault(merchantId, vaultId)     // Add merchant → vault mapping
getVaultAccountForMerchant(merchantId)         // Get vault for merchant
hasMerchantMapping(merchantId)                 // Check if merchant mapped
getDefaultVaultAccount()                       // Get default vault
```

---

## How It Works

1. **Payment Request** → Comes with `merchantId`
2. **Service Lookup** → Checks if merchant has custom vault
3. **Found** → Use merchant-specific vault
4. **Not Found** → Use default vault
5. **Process Payment** → Use vault in transaction

---

## Example Usage

```java
// Service handles all routing automatically
String vault = merchantVaultAccountService.getVaultAccountForMerchant(merchantId);

transactionGateway.transfer(
    card.getAccountId(),
    vault,  // ✅ Correct vault for any merchant
    amount,
    currency,
    idempotencyKey,
    TransactionType.CARD_PAYMENT
);
```

---

## Register New Merchant (Runtime)

```java
UUID newMerchant = UUID.fromString("...");
String newVault = "...";

merchantVaultAccountService.registerMerchantVault(newMerchant, newVault);
// ✅ Merchant now uses specific vault
```

---

## Default Behavior

```
If merchant NOT in mappings
    ↓
Returns: 99999999-9999-9999-9999-999999999998 (default vault)

If merchant IN mappings
    ↓
Returns: Merchant-specific vault account
```

---

## Files Modified

- CreditCardPaymentFlow.java
- DebitCardPaymentFlow.java

## Files Created

- MerchantVaultAccountService.java

---

## Ready to Build

```bash
cd backend\card-service
.\mvnw.cmd clean compile
```

**Status**: BUILD SUCCESS ✅

---

## Documentation

- **DYNAMIC_MERCHANT_VAULT_ROUTING.md** - Full guide
- **MERCHANT_VAULT_COMPLETE.md** - Quick summary
- **DYNAMIC_IMPLEMENTATION_FINAL.md** - Implementation details

---

✅ **Merchant vault account routing is now fully dynamic!**
