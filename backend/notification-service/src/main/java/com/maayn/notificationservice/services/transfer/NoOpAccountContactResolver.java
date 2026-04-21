package com.maayn.notificationservice.services.transfer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Primary
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
