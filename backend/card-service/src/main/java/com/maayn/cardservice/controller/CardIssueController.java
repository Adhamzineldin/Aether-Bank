package com.maayn.cardservice.controller;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.repository.CardRepository;
import com.maayn.cardservice.service.CardCreationService;
import com.maayn.cardservice.util.DemoPanGenerator;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.models.card.CardNetwork;
import maayn.veld.generated.models.card.CardSummary;
import maayn.veld.generated.models.card.CardType;
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

    @PostMapping
    public ResponseEntity<CardSummary> issueCard(@RequestBody IssueCardRequest request) {
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
        return ResponseEntity.status(HttpStatus.CREATED).body(CardMapper.toCardSummary(card));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CardSummary>> listCustomerCards(@PathVariable UUID customerId) {
        List<CardSummary> cards = cardRepository.findByCustomerId(customerId).stream()
                .map(CardMapper::toCardSummary)
                .toList();
        return ResponseEntity.ok(cards);
    }

    /**
     * Returns demo PAN + CVV for display (digits only). Omitted from the public
     * card summary until the user explicitly reveals them in the UI.
     */
    @GetMapping("/{cardId}/pan")
    public ResponseEntity<PanRevealResponse> revealPan(@PathVariable("cardId") UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        String digits = card.getPan() != null
                ? card.getPan()
                : DemoPanGenerator.syntheticLegacyPan(card.getId(), card.getCardNetwork(), card.getLastFourDigits());
        String cvv = card.getCvv() != null && !card.getCvv().isBlank()
                ? card.getCvv()
                : DemoPanGenerator.syntheticLegacyCvv(card.getId(), card.getCardNetwork());
        return ResponseEntity.ok(new PanRevealResponse(digits, cvv));
    }

    public record PanRevealResponse(String pan, String cvv) {}

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
