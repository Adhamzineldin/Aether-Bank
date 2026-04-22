package com.maayn.apigateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import javax.crypto.SecretKey;
import java.util.Optional;


public class JwtAuthFilter {

    private JwtAuthFilter() {}

    public static HandlerFilterFunction<ServerResponse, ServerResponse> withJwtAuth(PublicKeyProvider keyProvider) {
        return (request, next) -> {
            Optional<String> token = extractBearerToken(request);
            if (token.isEmpty()) {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
            }

            try {
                Claims claims = verifyToken(token.get(), keyProvider.getJwtSigningKey());
                String userId = claims.getSubject();
                if (userId == null || userId.isBlank()) {
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
                }
                String roles = claims.get("roles", String.class);
                return next.handle(injectUserContext(stripSpoofableHeaders(request), userId, roles));
            } catch (JwtException e) {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
            }
        };
    }

    private static ServerRequest stripSpoofableHeaders(ServerRequest request) {
        return ServerRequest.from(request)
                .headers(h -> {
                    h.remove("X-User-Id");
                    h.remove("X-User-Roles");
                })
                .build();
    }

    private static Optional<String> extractBearerToken(ServerRequest request) {
        String header = request.headers().asHttpHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return Optional.empty();
        return Optional.of(header.substring(7));
    }

    private static Claims verifyToken(String token, SecretKey signingKey) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Forwards trusted identity to downstream services. Values come only from the
     * verified JWT — never trust client-supplied {@code X-User-Id} / {@code X-User-Roles}.
     */
    private static ServerRequest injectUserContext(ServerRequest request, String userId, String roles) {
        return ServerRequest.from(request)
                .headers(h -> {
                    h.set("X-User-Id", userId);
                    if (roles != null && !roles.isBlank()) {
                        h.set("X-User-Roles", roles);
                    }
                })
                .build();
    }
}
