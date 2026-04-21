package com.maayn.notificationservice.services.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.UUID;
@Component
@ConditionalOnProperty(prefix = "notification.contact-lookup", name = "url-template", matchIfMissing = false)
@Slf4j
public class HttpAccountContactResolver implements AccountContactResolver {

    private final RestClient restClient;
    private final String urlTemplate;

    public HttpAccountContactResolver(
            @Value("${notification.contact-lookup.url-template}") String urlTemplate
    ) {
        this.urlTemplate = urlTemplate;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public Optional<AccountContactDetails> resolveByAccountId(UUID accountId) {
        String url = UriComponentsBuilder
                .fromUriString(urlTemplate.replace("{accountId}", accountId.toString()))
                .build(true)
                .toUriString();

        try {
            ContactResponse body = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(ContactResponse.class);
            if (body == null || body.email() == null || body.email().isBlank()) {
                log.warn("Contact lookup returned no email for account {}", accountId);
                return Optional.empty();
            }
            return Optional.of(new AccountContactDetails(body.userId(), body.email().trim()));
        } catch (Exception e) {
            log.error("Contact lookup failed for account {}: {}", accountId, e.getMessage());
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ContactResponse(UUID userId, String email) {
    }
}
