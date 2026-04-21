package com.maayn.notificationservice.services.transfer;

import java.util.Optional;
import java.util.UUID;
public interface AccountContactResolver {

    Optional<AccountContactDetails> resolveByAccountId(UUID accountId);
}
