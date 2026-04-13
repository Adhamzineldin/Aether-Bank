package com.maayn.transactionservice.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OutboxMessage Entity Unit Tests")
class OutboxMessageTest {

    @Test
    @DisplayName("Should construct with exchange, routing key, and payload")
    void constructor_setsFields() {
        OutboxMessage msg = new OutboxMessage("my.exchange", "my.routing.key", "{\"data\":\"value\"}");

        assertThat(msg.getExchange()).isEqualTo("my.exchange");
        assertThat(msg.getRoutingKey()).isEqualTo("my.routing.key");
        assertThat(msg.getPayload()).isEqualTo("{\"data\":\"value\"}");
    }

    @Test
    @DisplayName("Should initialize createdAt timestamp on construction")
    void constructor_setsCreatedAt() {
        LocalDateTime before = LocalDateTime.now();
        OutboxMessage msg = new OutboxMessage("exchange", "key", "payload");
        LocalDateTime after = LocalDateTime.now();

        assertThat(msg.getCreatedAt()).isNotNull();
        assertThat(msg.getCreatedAt()).isBetween(before, after.plusSeconds(1));
    }

    @Test
    @DisplayName("Should allow empty payload")
    void constructor_emptyPayload() {
        OutboxMessage msg = new OutboxMessage("exchange", "key", "");
        assertThat(msg.getPayload()).isEmpty();
    }

    @Test
    @DisplayName("Should allow large payload")
    void constructor_largePayload() {
        String largePayload = "x".repeat(100_000);
        OutboxMessage msg = new OutboxMessage("exchange", "key", largePayload);
        assertThat(msg.getPayload()).hasSize(100_000);
    }

    @Test
    @DisplayName("No-args constructor should create instance with null fields")
    void noArgsConstructor() {
        OutboxMessage msg = new OutboxMessage();
        assertThat(msg.getExchange()).isNull();
        assertThat(msg.getRoutingKey()).isNull();
        assertThat(msg.getPayload()).isNull();
        assertThat(msg.getId()).isNull();
    }
}

