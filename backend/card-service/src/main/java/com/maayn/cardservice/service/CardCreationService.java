package com.maayn.cardservice.service;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CreditCardDetailsEntity;
import com.maayn.cardservice.gateway.AccountGateway;
import com.maayn.cardservice.repository.CardRepository;
import com.maayn.cardservice.repository.CreditCardDetailsRepository;
import com.maayn.cardservice.util.DemoPanGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.models.card.CardNetwork;
import maayn.veld.generated.models.card.CardStatus;
import maayn.veld.generated.models.card.CardType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardCreationService {

    private static final BigDecimal MINIMUM_PAYMENT_RATE = new BigDecimal("0.02");

    private final CardRepository cardRepository;
    private final CreditCardDetailsRepository creditCardDetailsRepository;
    private final AccountGateway accountGateway;

    /**
     * Debit card links to an existing account — verifies the account exists first
     * and pulls the account's currency so future merchant payments target the
     * correct ledger row.
     */
    @Transactional
    public Card createDebitCard(UUID accountId, UUID customerId, CardNetwork network) {
        Objects.requireNonNull(accountId, "Account ID is required");
        Objects.requireNonNull(customerId, "Customer ID is required");
        Objects.requireNonNull(network, "Card network is required");
        accountGateway.verifyDebitAccountExists(accountId);
        // Best-effort: persist the linked account's currency so merchant
        // payments hit the right ledger row from the get-go. If the lookup
        // fails (network blip, controller schema drift, etc.) we don't fail
        // card issuance — CardPaymentService.resolveSettlementCurrency()
        // back-fills lazily on first payment.
        String accountCurrency = null;
        try {
            accountCurrency = accountGateway.fetchAccountCurrency(accountId);
        } catch (RuntimeException ex) {
            log.warn("Could not pre-fetch account currency for {}: {} — will back-fill on first payment", accountId, ex.getMessage());
        }
        Card card = buildBaseCard(accountId, customerId, CardType.DEBIT, network);
        card.setCurrency(accountCurrency);
        return cardRepository.save(card);
    }

    /**
     * Credit card provisions a dedicated account funded from the vault with the credit limit.
     * The account is created in the transaction service, then funded before the card is persisted.
     */
    @Transactional
    public Card createCreditCard(UUID customerId, CardNetwork network, BigDecimal creditLimit, BigDecimal annualInterestRate, String currency) {
        Objects.requireNonNull(customerId, "Customer ID is required");
        Objects.requireNonNull(network, "Card network is required");
        if (creditLimit == null || creditLimit.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Credit limit must be positive");
        if (annualInterestRate == null || annualInterestRate.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Annual interest rate cannot be negative");

        UUID creditAccountId = accountGateway.provisionCreditAccount(creditLimit, currency);
        Card baseCard = buildBaseCard(creditAccountId, customerId, CardType.CREDIT, network);
        baseCard.setCurrency(currency);
        Card card = cardRepository.save(baseCard);
        CreditCardDetailsEntity details = creditCardDetailsRepository.save(buildCreditDetails(card, creditLimit, annualInterestRate));
        card.setCreditDetails(details);
        return card;
    }

    private Card buildBaseCard(UUID accountId, UUID customerId, CardType type, CardNetwork network) {
        Card card = new Card();
        card.setAccountId(accountId);
        card.setCustomerId(customerId);
        card.setCardType(type);
        card.setCardNetwork(network);
        card.setStatus(CardStatus.ACTIVE);
        card.setCardToken("card_" + UUID.randomUUID().toString().replace("-", ""));
        String pan = DemoPanGenerator.generatePan(network);
        card.setPan(pan);
        card.setLastFourDigits(DemoPanGenerator.lastFourFromPan(pan));
        card.setCvv(DemoPanGenerator.generateCvv(network));
        LocalDate expiry = LocalDate.now().plusYears(5);
        card.setExpiryMonth(expiry.getMonthValue());
        card.setExpiryYear(expiry.getYear());
        return card;
    }

    private CreditCardDetailsEntity buildCreditDetails(Card card, BigDecimal creditLimit, BigDecimal annualInterestRate) {
        CreditCardDetailsEntity details = new CreditCardDetailsEntity();
        details.setCard(card);
        details.setCreditLimit(creditLimit);
        details.setAvailableCredit(creditLimit);
        details.setCurrentBalance(BigDecimal.ZERO);
        details.setMinimumPayment(creditLimit.multiply(MINIMUM_PAYMENT_RATE));
        details.setAnnualInterestRate(annualInterestRate);
        details.setBillingCycleDay(1);
        details.setPaymentDueDate(LocalDate.now().plusMonths(1));
        return details;
    }
}
