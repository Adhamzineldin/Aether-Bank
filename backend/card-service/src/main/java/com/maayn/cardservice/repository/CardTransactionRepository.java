package com.maayn.cardservice.repository;

import com.maayn.cardservice.entity.CardTransaction;
import maayn.veld.generated.models.card.CardTransactionStatus;
import maayn.veld.generated.models.card.CardTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CardTransactionRepository extends JpaRepository<CardTransaction, UUID> {

    Optional<CardTransaction> findByIdempotencyKey(String idempotencyKey);

    Page<CardTransaction> findByCardId(UUID cardId, Pageable pageable);

    Page<CardTransaction> findByCardIdAndStatus(UUID cardId, CardTransactionStatus status, Pageable pageable);

    Page<CardTransaction> findByCardIdAndType(UUID cardId, CardTransactionType type, Pageable pageable);

    Page<CardTransaction> findByCardIdAndStatusAndType(
            UUID cardId,
            CardTransactionStatus status,
            CardTransactionType type,
            Pageable pageable
    );
}
