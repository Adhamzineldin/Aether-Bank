package com.maayn.iamservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
    @Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at"),
    @Index(name = "idx_refresh_tokens_is_revoked", columnList = "is_revoked")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, unique = true, length = 512)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRevoked = false;

    @Column
    private LocalDateTime revokedAt;

    // Device Tracking
    @Column(length = 500)
    private String deviceInfo;

    @Column(length = 45)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime lastUsed;

    // Business Logic
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isValid() {
        return !isRevoked && !isExpired();
    }

    public void revoke() {
        this.isRevoked = true;
        this.revokedAt = LocalDateTime.now();
    }

    public void updateLastUsed() {
        this.lastUsed = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + id +
                ", userId=" + user.getId() +
                ", isRevoked=" + isRevoked +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
