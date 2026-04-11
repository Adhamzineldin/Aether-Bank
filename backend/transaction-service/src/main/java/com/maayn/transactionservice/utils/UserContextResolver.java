package com.maayn.transactionservice.utils;

import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.transaction.TransferRequest;
import maayn.veld.generated.sdk.iam.constants.AccountSystemConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class UserContextResolver {

   
    public UUID resolveUserId(TransferRequest request) {
        return extractFromHttpHeader()
                .or(() -> extractFromPayload(request))
                .orElseGet(this::fallbackToSystemUser);
    }
    

    private Optional<UUID> extractFromHttpHeader() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String userIdHeader = attributes.getRequest().getHeader("X-User-Id");
                if (userIdHeader != null && !userIdHeader.isBlank()) {
                    return Optional.of(UUID.fromString(userIdHeader));
                }
            }
        } catch (IllegalStateException e) {
            log.trace("No HTTP context available — SAGA path detected.");
        }
        return Optional.empty();
    }

    private Optional<UUID> extractFromPayload(TransferRequest request) {
        if (request.getInitiatedByUserId() != null) {
            return Optional.of(request.getInitiatedByUserId());
        }
        return Optional.empty();
    }

    private UUID fallbackToSystemUser() {
        log.debug("No userId found in context or payload — defaulting to SYSTEM_USER.");
        return AccountSystemConfig.SYSTEM_USER_ID;
    }
}