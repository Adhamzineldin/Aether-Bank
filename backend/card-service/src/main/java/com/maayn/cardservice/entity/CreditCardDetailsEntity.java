package com.maayn.cardservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "credit_card_details")
public class CreditCardDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false, unique = true)
    private Card card;

    @Column(name = "credit_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "available_credit", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableCredit;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "minimum_payment", nullable = false, precision = 19, scale = 2)
    private BigDecimal minimumPayment;

    @Column(name = "payment_due_date")
    private LocalDate paymentDueDate;

    @Column(name = "annual_interest_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal annualInterestRate;

    @Column(name = "billing_cycle_day", nullable = false)
    private Integer billingCycleDay;

    @Column(name = "last_statement_date")
    private LocalDate lastStatementDate;
}
