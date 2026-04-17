package com.maayn.cardservice.repository;

import com.maayn.cardservice.entity.CreditCardDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditCardDetailsRepository extends JpaRepository<CreditCardDetailsEntity, UUID> {
}
