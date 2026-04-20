package com.maayn.notificationservice.services.transfer;

import java.util.Optional;
import java.util.UUID;

/**
 * Resolves how to reach the account holder (typically the source account debited on a transfer).
 * Wire a real HTTP implementation once Account (or IAM) exposes a stable internal API.
 */
public interface AccountContactResolver {

    Optional<AccountContactDetails> resolveByAccountId(UUID accountId);
}
