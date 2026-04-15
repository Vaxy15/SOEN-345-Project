package com.soen345.ticketapp;

import com.soen345.ticketapp.util.InputValidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Input Validator Tests")
class InputValidatorTest {

    @Test
    @DisplayName("Valid email passes")
    void validEmail_returnsTrue() {
        assertTrue(InputValidator.isValidEmail("user@example.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"notanemail", "@nodomain", "a@b", ""})
    @DisplayName("Invalid emails fail")
    void invalidEmail_returnsFalse(String email) {
        assertFalse(InputValidator.isValidEmail(email));
    }

    @Test
    @DisplayName("Null email fails")
    void nullEmail_returnsFalse() {
        assertFalse(InputValidator.isValidEmail(null));
    }

    @Test
    @DisplayName("Valid phone passes")
    void validPhone_returnsTrue() {
        assertTrue(InputValidator.isValidPhone("+15141234567"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "123", ""})
    @DisplayName("Invalid phones fail")
    void invalidPhone_returnsFalse(String phone) {
        assertFalse(InputValidator.isValidPhone(phone));
    }

    @Test
    @DisplayName("Null phone fails")
    void nullPhone_returnsFalse() {
        assertFalse(InputValidator.isValidPhone(null));
    }

    @Test
    @DisplayName("Password 6+ chars passes")
    void validPassword_returnsTrue() {
        assertTrue(InputValidator.isValidPassword("abc123"));
    }

    @Test
    @DisplayName("Password under 6 chars fails")
    void shortPassword_returnsFalse() {
        assertFalse(InputValidator.isValidPassword("abc"));
    }

    @Test
    @DisplayName("Null password fails")
    void nullPassword_returnsFalse() {
        assertFalse(InputValidator.isValidPassword(null));
    }

    @Test
    @DisplayName("Matching passwords return true")
    void matchingPasswords_returnsTrue() {
        assertTrue(InputValidator.passwordsMatch("pass123", "pass123"));
    }

    @Test
    @DisplayName("Non-matching passwords return false")
    void nonMatchingPasswords_returnsFalse() {
        assertFalse(InputValidator.passwordsMatch("pass123", "different"));
    }

    @Test
    @DisplayName("Null password match returns false")
    void nullPasswordMatch_returnsFalse() {
        assertFalse(InputValidator.passwordsMatch(null, "pass123"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "50", "500"})
    @DisplayName("Valid seat counts pass")
    void validSeatCount_returnsTrue(String count) {
        assertTrue(InputValidator.isValidSeatCount(count));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc", ""})
    @DisplayName("Invalid seat counts fail")
    void invalidSeatCount_returnsFalse(String count) {
        assertFalse(InputValidator.isValidSeatCount(count));
    }

    @Test
    @DisplayName("Non-empty string passes")
    void nonEmptyString_returnsTrue() {
        assertTrue(InputValidator.isNonEmpty("Montreal"));
    }

    @Test
    @DisplayName("Blank string fails")
    void blankString_returnsFalse() {
        assertFalse(InputValidator.isNonEmpty("   "));
    }

    @Test
    @DisplayName("Null fails non-empty check")
    void nullString_returnsFalse() {
        assertFalse(InputValidator.isNonEmpty(null));
    }
}