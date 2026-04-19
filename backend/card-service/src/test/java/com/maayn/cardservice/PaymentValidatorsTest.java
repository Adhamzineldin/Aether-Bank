package com.maayn.cardservice.validators;

import com.maayn.cardservice.service.support.IbanValidator;
import com.maayn.cardservice.service.support.CvvValidator;
import com.maayn.cardservice.service.support.ExpiryDateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for payment validators (Single Responsibility testing)
 */
@DisplayName("Payment Validators Tests")
class PaymentValidatorsTest {

    private IbanValidator ibanValidator;
    private CvvValidator cvvValidator;
    private ExpiryDateValidator expiryDateValidator;

    @BeforeEach
    void setUp() {
        ibanValidator = new IbanValidator();
        cvvValidator = new CvvValidator();
        expiryDateValidator = new ExpiryDateValidator();
    }

    @DisplayName("IBAN Validator Tests")
    static class IbanValidatorTests {
        private IbanValidator ibanValidator;

        @BeforeEach
        void setUp() {
            ibanValidator = new IbanValidator();
        }

        @Test
        @DisplayName("Should accept valid German IBAN")
        void testValidGermanIban() {
            assertDoesNotThrow(() -> ibanValidator.validate("DE89370400440532013000"));
        }

        @Test
        @DisplayName("Should reject null IBAN")
        void testNullIban() {
            assertThrows(IllegalArgumentException.class, () -> ibanValidator.validate(null));
        }

        @Test
        @DisplayName("Should reject invalid checksum")
        void testInvalidChecksum() {
            assertThrows(IllegalArgumentException.class, 
                () -> ibanValidator.validate("DE89370400440532013001"));
        }
    }

    @DisplayName("CVV Validator Tests")
    static class CvvValidatorTests {
        private CvvValidator cvvValidator;

        @BeforeEach
        void setUp() {
            cvvValidator = new CvvValidator();
        }

        @Test
        @DisplayName("Should accept 3-digit CVV")
        void testValidCvv3Digit() {
            assertDoesNotThrow(() -> cvvValidator.validate("123"));
        }

        @Test
        @DisplayName("Should accept 4-digit CVV (AmEx)")
        void testValidCvv4Digit() {
            assertDoesNotThrow(() -> cvvValidator.validate("1234"));
        }

        @Test
        @DisplayName("Should reject non-numeric CVV")
        void testNonNumericCvv() {
            assertThrows(IllegalArgumentException.class, () -> cvvValidator.validate("ABC"));
        }

        @Test
        @DisplayName("Should reject invalid length CVV")
        void testInvalidLengthCvv() {
            assertThrows(IllegalArgumentException.class, () -> cvvValidator.validate("12"));
        }
    }

    @DisplayName("Expiry Date Validator Tests")
    static class ExpiryDateValidatorTests {
        private ExpiryDateValidator expiryDateValidator;

        @BeforeEach
        void setUp() {
            expiryDateValidator = new ExpiryDateValidator();
        }

        @Test
        @DisplayName("Should accept valid future expiry date")
        void testValidFutureExpiryDate() {
            assertDoesNotThrow(() -> expiryDateValidator.validate("12/28"));
        }

        @Test
        @DisplayName("Should reject invalid format")
        void testInvalidFormat() {
            assertThrows(IllegalArgumentException.class, 
                () -> expiryDateValidator.validate("2028-12"));
        }

        @Test
        @DisplayName("Should reject expired date")
        void testExpiredDate() {
            assertThrows(IllegalArgumentException.class, 
                () -> expiryDateValidator.validate("01/20"));
        }
    }
}
