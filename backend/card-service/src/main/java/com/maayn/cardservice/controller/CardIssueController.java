package com.maayn.cardservice.controller;

import com.maayn.cardservice.audit.AuditPublisher;
import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.repository.CardRepository;
import com.maayn.cardservice.service.CardCreationService;
import com.maayn.cardservice.util.DemoPanGenerator;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.models.card.CardNetwork;
import maayn.veld.generated.models.card.CardSummary;
import maayn.veld.generated.models.card.CardType;
import maayn.veld.generated.models.card.PanRevealResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Hand-written companion to the auto-generated {@code CardController}.
 * Provides the card issuance + customer-listing endpoints that the Veld spec
 * does not yet describe, so the frontend "Issue card" and "My cards" flows
 * can hit real HTTP endpoints instead of stubbing.
 */
@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardIssueController {

    private static final BigDecimal DEFAULT_CREDIT_LIMIT = new BigDecimal("10000");
    private static final BigDecimal DEFAULT_CREDIT_APR = new BigDecimal("0.1899");

    private final CardCreationService cardCreationService;
    private final CardRepository cardRepository;
    private final AuditPublisher auditPublisher;

    @PostMapping
    public ResponseEntity<CardSummary> issueCard(@RequestBody IssueCardRequest request) {
        try {
            Card card;
            if (request.cardType() == CardType.CREDIT) {
                card = cardCreationService.createCreditCard(
                        request.customerId(),
                        request.cardNetwork(),
                        request.creditLimit() != null ? request.creditLimit() : DEFAULT_CREDIT_LIMIT,
                        request.annualInterestRate() != null ? request.annualInterestRate() : DEFAULT_CREDIT_APR,
                        request.currency() != null ? request.currency() : "USD"
                );
            } else {
                if (request.accountId() == null) {
                    throw new IllegalArgumentException("accountId is required for DEBIT cards");
                }
                card = cardCreationService.createDebitCard(request.accountId(), request.customerId(), request.cardNetwork());
            }
            auditPublisher.publishSuccess(
                    "ISSUE_CARD",
                    request.customerId(),
                    String.format("Issued %s %s card %s linked to account %s",
                            request.cardType(), request.cardNetwork(), card.getId(), card.getAccountId()));
            return ResponseEntity.status(HttpStatus.CREATED).body(CardMapper.toCardSummary(card));
        } catch (RuntimeException ex) {
            auditPublisher.publishFailure(
                    "ISSUE_CARD",
                    request.customerId(),
                    String.format("Failed to issue %s %s card: %s",
                            request.cardType(), request.cardNetwork(), ex.getMessage()));
            throw ex;
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CardSummary>> listCustomerCards(@PathVariable UUID customerId) {
        List<CardSummary> cards = cardRepository.findByCustomerId(customerId).stream()
                .map(CardMapper::toCardSummary)
                .toList();
        return ResponseEntity.ok(cards);
    }



    public record IssueCardRequest(
            UUID customerId,
            UUID accountId,
            CardType cardType,
            CardNetwork cardNetwork,
            String currency,
            BigDecimal creditLimit,
            BigDecimal annualInterestRate
    ) {}
}
