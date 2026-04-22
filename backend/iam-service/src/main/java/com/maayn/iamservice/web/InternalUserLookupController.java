package com.maayn.iamservice.web;

import com.maayn.iamservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Internal service-to-service endpoints. Mounted under {@code /api/internal/**}
 * so they can be {@code permitAll}-ed in {@code SecurityConfig} (no JWT
 * required) and exposed only inside the docker network — the api-gateway is
 * configured to not route external traffic to {@code /api/internal/**}.
 *
 * <p>Currently used by notification-service to look up a user's email by id
 * (via account-service's {@code /api/accounts/{accountId}/contact} which
 * resolves account→customerId→email).
 */
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalUserLookupController {

    private final UserRepository userRepository;

    public record UserContactResponse(UUID userId, String email) { }

    @GetMapping("/users/{id}/email")
    public ResponseEntity<UserContactResponse> getUserEmail(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(new UserContactResponse(u.getId(), u.getEmail())))
                .orElse(ResponseEntity.notFound().build());
    }
}

