package com.soen345.ticketapp.util;

public class InputValidator {

    public static boolean isValidEmail(String email) {
        return email != null
                && email.contains("@")
                && email.contains(".")
                && email.trim().length() > 5;
    }

    public static boolean isValidPhone(String phone) {
        return phone != null
                && phone.matches("^\\+?[1-9]\\d{9,14}$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean passwordsMatch(String pwd, String confirm) {
        return pwd != null && pwd.equals(confirm);
    }

    public static boolean isValidSeatCount(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        try {
            return Integer.parseInt(input.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }
}