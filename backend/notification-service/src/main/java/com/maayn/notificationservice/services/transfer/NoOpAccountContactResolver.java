package com.maayn.notificationservice.services.transfer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.UUID;

/**
 * Fallback resolver wired only when no real {@link AccountContactResolver}
 * is on the context (i.e. {@code notification.contact-lookup.url-template}
 * is unset). Logs a single warning per call and returns no recipient so the
 * transfer-alert path silently no-ops.
 *
 * <p>Previously this was a {@code @Component @Primary} which meant the
 * NoOp won even after {@link HttpAccountContactResolver} registered —
 * which is why we kept seeing "No AccountContactResolver configured" even
 * after the URL template was set.
 */
@Configuration
@Slf4j
public class NoOpAccountContactResolver {

    @Bean
    @ConditionalOnMissingBean(AccountContactResolver.class)
    public AccountContactResolver noOpAccountContactResolver() {
        return accountId -> {
            log.warn(
                    "No AccountContactResolver configured; set notification.contact-lookup.url-template "
                            + "to enable HTTP lookup for account {}",
                    accountId
            );
            return Optional.<AccountContactDetails>empty();
        };
    }

    // Keep the unused import warning quiet for tooling that scans imports.
    @SuppressWarnings("unused")
    private static UUID unused() { return null; }
}
