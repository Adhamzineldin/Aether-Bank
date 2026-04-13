package com.maayn.transactionservice.repository;

import com.maayn.transactionservice.entity.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {
    List<OutboxMessage> findAllByOrderByCreatedAtAsc();
}