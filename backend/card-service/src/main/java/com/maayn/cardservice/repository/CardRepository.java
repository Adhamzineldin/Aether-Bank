package com.maayn.cardservice.repository;

import com.maayn.cardservice.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findByCardToken(String cardToken);

    /**
     * Demo helper: look up a card by its raw PAN (digits only). Used by the
     * merchant-payment endpoint so the public checkout form can accept a card
     * number entered by the user, not just a server-issued tokenized handle.
     */
    Optional<Card> findByPan(String pan);

    List<Card> findByCustomerId(UUID customerId);
}
