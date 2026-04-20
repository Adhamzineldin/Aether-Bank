package com.maayn.iamservice.repository;

import com.maayn.iamservice.domain.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {
    
    Optional<OtpCode> findByUserIdAndCodeAndIsUsedFalse(UUID userId, String code);
    
    List<OtpCode> findByUserIdAndPurpose(UUID userId, String purpose);
    
    List<OtpCode> findByExpiresAtBeforeAndIsUsedFalse(LocalDateTime dateTime);
}
