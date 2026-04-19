package com.maayn.cardservice.service.usecase;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.entity.CardTransaction;
import com.maayn.cardservice.exception.TransactionGatewayException;
import com.maayn.cardservice.mapper.CardMapper;
import com.maayn.cardservice.repository.CardRepository;
import com.maayn.cardservice.repository.CardTransactionRepository;
import com.maayn.cardservice.service.support.CardAccessService;
import com.maayn.cardservice.service.support.CardRulesValidator;
import com.maayn.cardservice.service.support.CardTransactionFactory;
import com.maayn.cardservice.service.support.CreditBalanceService;
import com.maayn.cardservice.service.support.MerchantPaymentService;
import com.maayn.cardservice.service.support.TransactionGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Executes merchant purchase requests against a card (SOLID: Single Responsibility).
 * Delegates to MerchantPaymentService for payment flow processing.
 * Uses proper dependency injection with @RequiredArgsConstructor.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CardPaymentService {

    private final CardAccessService cardAccessService;
    private final CardRulesValidator cardRulesValidator;
    private final TransactionGateway transactionGateway;
    private final CardTransactionFactory cardTransactionFactory;
    private final CreditBalanceService creditBalanceService;
    private final CardTransactionRepository cardTransactionRepository;
    private final CardRepository cardRepository;
    private final MerchantPaymentService merchantPaymentService;

    @Transactional
    public CardTransactionResponse process(MerchantPaymentRequest input) throws ProcessMerchantPaymentException, Exception {
        try {
            log.info("Processing merchant payment for merchant: {}", input.getMerchantId());
            
            cardRulesValidator.validateMerchantPaymentRequest(input);

            // Reuse an existing transaction if the same idempotency key was already processed successfully
            String idempotencyKey = resolvePaymentIdempotencyKey(input);
            CardTransaction cached = cardAccessService.findTransactionByIdempotencyKey(idempotencyKey).orElse(null);
            if (cached != null) {
                log.info("Returning cached transaction for idempotency key: {}", idempotencyKey);
                return CardMapper.toTransactionResponse(cached);
            }

            // Delegate to merchant payment service which handles both CREDIT and DEBIT flows
            // Note: IBAN, CVV, Expiry are validated within the payment flows if provided
            return merchantPaymentService.processMerchantPayment(
                    input.getCardToken(),
                    input.getMerchantId().toString(),
                    input.getAmount(),
                    input.getCurrency(),
                    idempotencyKey
            );
        } catch (TransactionGatewayException ex) {
            throw mapPaymentGatewayFailure(ex);
        }
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
