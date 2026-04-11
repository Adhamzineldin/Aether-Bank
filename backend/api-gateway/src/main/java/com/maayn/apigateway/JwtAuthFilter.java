package com.maayn.apigateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.security.interfaces.RSAPublicKey;
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
                Claims claims = verifyToken(token.get(), keyProvider.getPublicKey());
                String userId = claims.getSubject();
                if (userId == null || userId.isBlank()) {
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
                }
                return next.handle(injectUserId(stripSpoofableHeaders(request), userId));
            } catch (JwtException e) {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
            }
        };
    }

    private static ServerRequest stripSpoofableHeaders(ServerRequest request) {
        return ServerRequest.from(request)
                .headers(h -> h.remove("X-User-Id"))
                .build();
    }

    private static Optional<String> extractBearerToken(ServerRequest request) {
        String header = request.headers().asHttpHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return Optional.empty();
        return Optional.of(header.substring(7));
    }

    private static Claims verifyToken(String token, RSAPublicKey publicKey) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static ServerRequest injectUserId(ServerRequest request, String userId) {
        return ServerRequest.from(request)
                .headers(h -> h.set("X-User-Id", userId))
                .build();
    }
}
