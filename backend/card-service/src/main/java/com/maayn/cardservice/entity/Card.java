package com.maayn.cardservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import maayn.veld.generated.models.card.CardNetwork;
import maayn.veld.generated.models.card.CardStatus;
import maayn.veld.generated.models.card.CardType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "card_token", nullable = false, unique = true, length = 128)
    private String cardToken;

    @Column(name = "last_four_digits", nullable = false, length = 4)
    private String lastFourDigits;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, length = 32)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_network", nullable = false, length = 32)
    private CardNetwork cardNetwork;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CardStatus status;

    @Column(name = "expiry_month", nullable = false)
    private Integer expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private Integer expiryYear;

    @CreationTimestamp
    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "block_reason")
    private String blockReason;

    @OneToOne(mappedBy = "card", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private CreditCardDetailsEntity creditDetails;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL)
    private List<CardTransaction> transactions = new ArrayList<>();
}
