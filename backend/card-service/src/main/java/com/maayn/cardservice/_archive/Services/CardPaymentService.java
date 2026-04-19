package com.maayn.cardservice.Services;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.exception.TransactionGatewayException;
import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.repository.CardRepository;
import com.maayn.cardservice.repository.CardTransactionRepository;
import com.maayn.cardservice.Services.CardAccessService;
import com.maayn.cardservice.Validators.CardRulesValidator;
import com.maayn.cardservice.Services.CardTransactionFactory;
import com.maayn.cardservice.Services.CreditBalanceService;
import com.maayn.cardservice.Gateways.TransactionGateway;
import maayn.veld.generated.errors.CardErrors;
import maayn.veld.generated.errors.ProcessMerchantPaymentException;
import maayn.veld.generated.models.card.*;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionResponse;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
/**
 * Executes merchant purchase requests against a card.
 * The flow validates input, enforces card rules, calls the transaction service, then stores the local card transaction.
 */
public class CardPaymentService {

    private final CardAccessService cardAccessService;
    private final CardRulesValidator cardRulesValidator;
    private final TransactionGateway transactionGateway;
    private final CardTransactionFactory cardTransactionFactory;
    private final CreditBalanceService creditBalanceService;
    private final CardTransactionRepository cardTransactionRepository;
    private final CardRepository cardRepository;

    
    //TODO: USE Dependency Injections like professionals
    public CardPaymentService(
            CardAccessService cardAccessService,
            CardRulesValidator cardRulesValidator,
            TransactionGateway transactionGateway,
            CardTransactionFactory cardTransactionFactory,
            CreditBalanceService creditBalanceService,
            CardTransactionRepository cardTransactionRepository, CardRepository cardRepository
    ) {
        this.cardAccessService = cardAccessService;
        this.cardRulesValidator = cardRulesValidator;
        this.transactionGateway = transactionGateway;
        this.cardTransactionFactory = cardTransactionFactory;
        this.creditBalanceService = creditBalanceService;
        this.cardTransactionRepository = cardTransactionRepository;
        this.cardRepository = cardRepository;
    }

    @Transactional
    public CardTransactionResponse process(MerchantPaymentRequest input) throws ProcessMerchantPaymentException, Exception {
        cardRulesValidator.validateMerchantPaymentRequest(input);

        // Reuse an existing transaction if the same idempotency key was already processed successfully.
        String idempotencyKey = resolvePaymentIdempotencyKey(input);
        CardTransaction cached = cardAccessService.findTransactionByIdempotencyKey(idempotencyKey).orElse(null);
        if (cached != null) {
            return CardMapper.toTransactionResponse(cached);
        }

//        Card card = cardAccessService.getCardByToken(input.getCardToken());
//        cardRulesValidator.validateCardForPayment(card, input.getCurrency(), input.getAmount());
        //TODO: implement actual flow to create a card 
        Card card = new Card();
        card.setAccountId(UUID.fromString("99999999-9999-9999-9999-999999999999"));
        card.setCardToken(input.getCardToken());
        card.setLastFourDigits("1234");
        card.setCardType(CardType.DEBIT);
        card.setActivatedAt(LocalDateTime.now().minusDays(30));
        card.setExpiryMonth(12);
        card.setExpiryYear(2025);
        card.setIssuedAt(LocalDateTime.now().minusDays(365));
        card.setCardNetwork(CardNetwork.VISA);
        card.setCustomerId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);

        TransactionResponse transferResult;
        try {
            // Move funds from the card-linked account into the bank cash vault through the transaction service.
            transferResult = transactionGateway.transfer(
                    card.getAccountId(),
                    SystemAccounts.CASH_VAULT_ID,
                    input.getAmount(),
                    input.getCurrency(),
                    idempotencyKey,
                    TransactionType.CARD_PAYMENT
            );
        } catch (TransactionGatewayException ex) {
            throw mapPaymentGatewayFailure(ex);
        }

        CardTransaction transaction = cardTransactionFactory.createPurchase(
                card,
                input.getMerchantId(),
                idempotencyKey,
                transferResult.getReferenceNumber(),
                input.getAmount(),
                input.getCurrency()
        );

        // Credit cards also maintain an internal credit ledger that mirrors the approved charge.
        creditBalanceService.applyCharge(card, input.getAmount());
        cardTransactionRepository.save(transaction);
        return CardMapper.toTransactionResponse(transaction);
    }

    private ProcessMerchantPaymentException mapPaymentGatewayFailure(TransactionGatewayException ex) throws ProcessMerchantPaymentException {
        return switch (ex.getReason()) {
            case INVALID_AMOUNT -> CardErrors.processMerchantPayment.invalidAmount(ex.getMessage());
            case INSUFFICIENT_FUNDS -> CardErrors.processMerchantPayment.insufficientCredit(ex.getMessage());
            case UNKNOWN -> throw ex;
        };
    }

    private String resolvePaymentIdempotencyKey(MerchantPaymentRequest input) {
        if (input.getIdempotencyKey() != null && !input.getIdempotencyKey().trim().isEmpty()) {
            return input.getIdempotencyKey();
        }
        // Fall back to a deterministic key so retries without an explicit key still deduplicate.
        return "card-payment-" + input.getCardToken() + "-" + cardRulesValidator.normalizeCurrency(input.getCurrency()) + "-" + input.getAmount();
    }
}
