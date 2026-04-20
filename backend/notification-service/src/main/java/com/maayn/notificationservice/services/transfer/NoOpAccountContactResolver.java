package com.maayn.notificationservice.services.transfer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnMissingBean(AccountContactResolver.class)
@Slf4j
public class NoOpAccountContactResolver implements AccountContactResolver {

    @Override
    public Optional<AccountContactDetails> resolveByAccountId(UUID accountId) {
        log.warn(
                "No AccountContactResolver configured; set notification.contact-lookup.url-template "
                        + "to enable HTTP lookup for account {}",
                accountId
        );
        return Optional.empty();
    }
}
