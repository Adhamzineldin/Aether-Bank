package com.maayn.cardservice.service.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Merchant Vault Account Service (SOLID: Single Responsibility).
 * Manages merchant-to-vault account mapping for payment routing.
 * Supports dynamic merchant routing for transaction processing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantVaultAccountService {

    // In-memory cache for merchant vault mappings (production would use database)
    private static final Map<String, String> MERCHANT_VAULT_MAPPING = new HashMap<>();

    static {
        // Initialize default merchant vault accounts
        // In production, these would be persisted in a database and loaded dynamically
        initializeDefaultMerchants();
    }

    /**
     * Get the vault account for a specific merchant.
     * Falls back to default vault if merchant not found.
     *
     * @param merchantId the merchant UUID
     * @return the vault account UUID as String
     */
    public String getVaultAccountForMerchant(UUID merchantId) {
        if (merchantId == null) {
            log.warn("Merchant ID is null, using default vault account");
            return getDefaultVaultAccount();
        }

        String merchantIdStr = merchantId.toString();
        String vaultAccount = MERCHANT_VAULT_MAPPING.getOrDefault(
                merchantIdStr,
                getDefaultVaultAccount()
        );

        log.debug("Mapped merchant {} to vault account {}", merchantIdStr, vaultAccount);
        return vaultAccount;
    }

    /**
     * Get the default vault account for unmapped merchants.
     */
    public String getDefaultVaultAccount() {
        // Default vault account used when merchant-specific routing is not available
        return "99999999-9999-9999-9999-999999999998";
    }

    /**
     * Register a merchant to a specific vault account.
     * Can be used at runtime to add new merchant mappings.
     *
     * @param merchantId the merchant UUID
     * @param vaultAccountId the vault account UUID
     */
    public void registerMerchantVault(UUID merchantId, String vaultAccountId) {
        if (merchantId == null || vaultAccountId == null) {
            log.warn("Cannot register null merchant or vault account");
            return;
        }

        String merchantIdStr = merchantId.toString();
        MERCHANT_VAULT_MAPPING.put(merchantIdStr, vaultAccountId);
        log.info("Registered merchant {} with vault account {}", merchantIdStr, vaultAccountId);
    }

    /**
     * Check if a merchant has a custom vault account mapping.
     */
    public boolean hasMerchantMapping(UUID merchantId) {
        return merchantId != null && MERCHANT_VAULT_MAPPING.containsKey(merchantId.toString());
    }

    /**
     * Initialize default merchant vault mappings.
     * In production, this would load from a database or configuration service.
     */
    private static void initializeDefaultMerchants() {
        // Example mappings - in production these would come from a database or config
        // Format: merchant-uuid -> vault-account-uuid

        // These are placeholder UUIDs for demonstration
        MERCHANT_VAULT_MAPPING.put(
                "550e8400-e29b-41d4-a716-446655440000",  // Example merchant 1
                "550e8400-e29b-41d4-a716-446655440001"   // Their vault account
        );

        MERCHANT_VAULT_MAPPING.put(
                "6ba7b810-9dad-11d1-80b4-00c04fd430c8",  // Example merchant 2
                "6ba7b810-9dad-11d1-80b4-00c04fd430c9"   // Their vault account
        );

        log.info("Initialized merchant vault account mappings");
    }
}
