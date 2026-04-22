package com.maayn.accountservice.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maayn.accountservice.entity.BankAccount;
import com.maayn.accountservice.exception.AccountNotFoundException;
import com.maayn.accountservice.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * Resolves the contact (user id + email) for an account. Used by
 * notification-service's {@code HttpAccountContactResolver} so transfer
 * alert emails can be addressed to the right person.
 *
 * <p>Joins {@code account.customerId} → iam-service {@code /api/internal/users/{id}/email}.
 * Lives on account-service rather than iam directly because the caller starts
 * with an {@code accountId} and only account-service knows the
 * account→customer mapping.
 */
@RestController
@RequestMapping({"/api/accounts", "/api/accounts_service/account"})
@RequiredArgsConstructor
@Slf4j
public class AccountContactController {

    private final BankAccountRepository bankAccountRepository;
    private final RestClient restClient = RestClient.builder().build();

    @Value("${iam.service.base-url:${IAM_SERVICE_URL:http://iam-service:8085}}")
    private String iamBaseUrl;

    public record ContactResponse(UUID userId, String email) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record IamEmailResponse(UUID userId, String email) { }

    @GetMapping("/{accountId}/contact")
    public ResponseEntity<ContactResponse> getAccountContact(@PathVariable UUID accountId) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        UUID customerId = account.getCustomerId();
        if (customerId == null) {
            log.warn("Account {} has no customerId; cannot resolve contact", accountId);
            return ResponseEntity.notFound().build();
        }

        try {
            IamEmailResponse iam = restClient.get()
                    .uri(iamBaseUrl + "/api/internal/users/{id}/email", customerId)
                    .retrieve()
                    .body(IamEmailResponse.class);
            if (iam == null || iam.email() == null || iam.email().isBlank()) {
                log.warn("iam-service returned no email for user {} (account {})", customerId, accountId);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new ContactResponse(customerId, iam.email().trim()));
        } catch (Exception ex) {
            log.warn("Contact resolution failed for account {}: {}", accountId, ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}

