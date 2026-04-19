package com.maayn.cardservice.service;

import com.maayn.cardservice.entity.Card;
import com.maayn.cardservice.service.support.PaymentFlow;
import com.maayn.cardservice.service.support.PaymentFlowFactory;
import com.maayn.cardservice.service.support.PaymentFlowType;
import com.maayn.cardservice.service.support.CreditCardPaymentFlow;
import com.maayn.cardservice.service.support.DebitCardPaymentFlow;
import maayn.veld.generated.models.card.CardType;
import maayn.veld.generated.models.card.CardStatus;
import maayn.veld.generated.models.card.CardNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for PaymentFlowFactory (Factory Pattern testing)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Flow Factory Tests")
class PaymentFlowFactoryTest {

    @Mock
    private CreditCardPaymentFlow creditCardPaymentFlow;

    @Mock
    private DebitCardPaymentFlow debitCardPaymentFlow;

    @InjectMocks
    private PaymentFlowFactory paymentFlowFactory;

    private Card creditCard;
    private Card debitCard;

    @BeforeEach
    void setUp() {
        creditCard = createCard(CardType.CREDIT);
        debitCard = createCard(CardType.DEBIT);
    }

    @Test
    @DisplayName("Should create CREDIT payment flow for credit card")
    void testCreateCreditPaymentFlow() {
        PaymentFlow flow = paymentFlowFactory.createPaymentFlow(creditCard);
        assertEquals(PaymentFlowType.CREDIT_CARD, flow.getFlowType());
    }

    @Test
    @DisplayName("Should create DEBIT payment flow for debit card")
    void testCreateDebitPaymentFlow() {
        PaymentFlow flow = paymentFlowFactory.createPaymentFlow(debitCard);
        assertEquals(PaymentFlowType.DEBIT_CARD, flow.getFlowType());
    }

    @Test
    @DisplayName("Should throw exception for null card")
    void testNullCard() {
        assertThrows(IllegalArgumentException.class, 
            () -> paymentFlowFactory.createPaymentFlow(null));
    }

    private Card createCard(CardType cardType) {
        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setAccountId(UUID.randomUUID());
        card.setCustomerId(UUID.randomUUID());
        card.setCardToken("test-token-" + cardType);
        card.setLastFourDigits("1234");
        card.setCardType(cardType);
        card.setCardNetwork(CardNetwork.VISA);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpiryMonth(12);
        card.setExpiryYear(2025);
        card.setIssuedAt(LocalDateTime.now());
        return card;
    }
}
