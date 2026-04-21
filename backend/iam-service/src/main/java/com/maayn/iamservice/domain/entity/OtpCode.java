package com.maayn.iamservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp_codes", indexes = {
    @Index(name = "idx_otp_codes_user_id", columnList = "user_id"),
    @Index(name = "idx_otp_codes_expires_at", columnList = "expires_at"),
    @Index(name = "idx_otp_codes_purpose", columnList = "purpose")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OtpCode {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false, length = 50)
    private String purpose;  // LOGIN, PASSWORD_RESET, EMAIL_VERIFICATION

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    @Column
    private LocalDateTime usedAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxAttempts = 3;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Business Logic
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isValid() {
        return !isUsed && !isExpired() && attemptCount < maxAttempts;
    }

    public void recordAttempt() {
        this.attemptCount++;
    }

    public boolean hasAttemptsRemaining() {
        return attemptCount < maxAttempts;
    }

    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "OtpCode{" +
                "id=" + id +
                ", purpose='" + purpose + '\'' +
                ", isUsed=" + isUsed +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
