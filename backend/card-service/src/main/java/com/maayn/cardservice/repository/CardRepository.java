package com.maayn.cardservice.repository;

import com.maayn.cardservice.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findByCardToken(String cardToken);

    List<Card> findByCustomerId(UUID customerId);
}
