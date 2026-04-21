package com.maayn.iamservice.repository;

import com.maayn.iamservice.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    
    List<RefreshToken> findByUserId(UUID userId);
    
    List<RefreshToken> findByUserIdAndIsRevokedFalse(UUID userId);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < ?1 AND rt.isRevoked = false")
    List<RefreshToken> findExpiredTokens(LocalDateTime now);
}
