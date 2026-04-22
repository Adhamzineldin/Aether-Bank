package com.maayn.cardservice.service;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.exception.TransactionGatewayException;
import com.maayn.cardservice.gateway.AccountGateway;
import com.maayn.cardservice.gateway.TransactionGateway;
import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.payment.PaymentFlowFactory;
import com.maayn.cardservice.repository.CardRepository;
import com.maayn.cardservice.repository.CardTransactionRepository;
import com.maayn.cardservice.validator.CardRulesValidator;
import lombok.RequiredArgsConstructor;
import maayn.veld.generated.errors.CardErrors;
import maayn.veld.generated.errors.ProcessMerchantPaymentException;
import maayn.veld.generated.models.card.CardTransactionResponse;
import maayn.veld.generated.models.card.MerchantPaymentRequest;
import maayn.veld.generated.sdk.account.constants.SystemAccounts;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionResponse;
import maayn.veld.generated.sdk.transaction.models.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardPaymentService {

    private final CardAccessService cardAccessService;
    private final CardRulesValidator cardRulesValidator;
    private final TransactionGateway transactionGateway;
    private final AccountGateway accountGateway;
    private final CardTransactionFactory cardTransactionFactory;
    private final PaymentFlowFactory paymentFlowFactory;
    private final CardTransactionRepository cardTransactionRepository;
    private final CardRepository cardRepository;

    @Transactional
    public CardTransactionResponse process(MerchantPaymentRequest input) throws ProcessMerchantPaymentException {
        cardRulesValidator.validateMerchantPaymentRequest(input);
        String idempotencyKey = resolveIdempotencyKey(input);

        var cached = cardAccessService.findTransactionByIdempotencyKey(idempotencyKey);
        if (cached.isPresent()) return CardMapper.toTransactionResponse(cached.get());
        return executePayment(input, idempotencyKey);
    }

    private CardTransactionResponse executePayment(MerchantPaymentRequest input, String idempotencyKey) {
        Card card = cardAccessService.getCardByToken(input.getCardToken());
        cardRulesValidator.validateCardForPayment(card, input.getCurrency(), input.getAmount());

        // Always settle in the card's own ledger currency. The currency on
        // the request is treated as a display hint from the merchant page;
        // using card.currency guarantees a (accountId, currency) ledger row
        // exists in transaction-service and avoids "wallets do not exist"
        // errors when the merchant's currency differs from the card's.
        String settlementCurrency = resolveSettlementCurrency(card, input.getCurrency());

        TransactionResponse transfer = transferFunds(card, input, settlementCurrency, idempotencyKey);
        CardTransaction tx = cardTransactionFactory.createPurchase(
                card, input.getMerchantId(), idempotencyKey,
                transfer.getReferenceNumber(), input.getAmount(), settlementCurrency);

        paymentFlowFactory.create(card).applyPostTransferEffects(card, input.getAmount());
        cardTransactionRepository.save(tx);
        return CardMapper.toTransactionResponse(tx);
    }

    /**
     * Returns the ISO currency of the card's underlying ledger account.
     * Back-fills {@code card.currency} for legacy rows on first use so future
     * payments skip the lookup.
     */
    private String resolveSettlementCurrency(Card card, String requestedCurrency) {
        if (card.getCurrency() != null && !card.getCurrency().isBlank()) {
            return card.getCurrency();
        }
        try {
            String fetched = accountGateway.fetchAccountCurrency(card.getAccountId());
            card.setCurrency(fetched);
            cardRepository.save(card);
            return fetched;
        } catch (RuntimeException ignored) {
            // Account-service may not know about credit-card accounts (they
            // live only in the ledger); fall back to whatever the merchant
            // requested. If the ledger row also doesn't exist for that
            // currency the transfer call below surfaces a clearer error.
            return cardRulesValidator.normalizeCurrency(requestedCurrency);
        }
    }

    private TransactionResponse transferFunds(Card card, MerchantPaymentRequest input,
                                              String settlementCurrency, String idempotencyKey) {
        try {
            return transactionGateway.transfer(
                    card.getAccountId(), SystemAccounts.CASH_VAULT_ID,
                    input.getAmount(), settlementCurrency, idempotencyKey, TransactionType.CARD_PAYMENT);
        } catch (TransactionGatewayException ex) {
            throw mapGatewayError(ex);
        }
    }

    private ProcessMerchantPaymentException mapGatewayError(TransactionGatewayException ex) {
        return switch (ex.getReason()) {
            case INVALID_AMOUNT -> CardErrors.processMerchantPayment.invalidAmount(ex.getMessage());
            case INSUFFICIENT_FUNDS -> CardErrors.processMerchantPayment.insufficientCredit(ex.getMessage());
            case UNKNOWN -> throw ex;
        };
    }

    private String resolveIdempotencyKey(MerchantPaymentRequest input) {
        if (input.getIdempotencyKey() != null && !input.getIdempotencyKey().isBlank()) {
            return input.getIdempotencyKey();
        }
        return "card-payment-" + input.getCardToken() + "-" + cardRulesValidator.normalizeCurrency(input.getCurrency()) + "-" + input.getAmount();
    }

    /** Audit-friendly preview of a card token / PAN: only the trailing 4 chars. */
    private static String tokenSuffix(String token) {
        if (token == null || token.length() <= 4) return "****";
        return "****" + token.substring(token.length() - 4);
    }
}
