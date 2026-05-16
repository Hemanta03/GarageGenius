package com.garagegenius.util;

/**
 * Common validation/sanitization helpers used by controllers/services.
 *
 * <p>These checks are intentionally lightweight and suitable for coursework-level
 * form validation (server-side). Frontend pages may still add client-side checks,
 * but backend validation is authoritative.</p>
 */
public class ValidationUtil {

    private ValidationUtil() {
    }

    /**
     * @param value string to check
     * @return {@code true} if value is null/blank after trimming
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Basic email validation suitable for coursework forms.
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        String trimmed = email.trim();
        return trimmed.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Allows digits and common separators, but enforces a 10–15 digit count.
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) return false;
        String trimmed = phone.trim();
        if (!trimmed.matches("^[0-9+\\-\\s()]{7,25}$")) return false;
        String digitsOnly = trimmed.replaceAll("\\D", "");
        return digitsOnly.length() >= 10 && digitsOnly.length() <= 15;
    }

    /**
     * Allows letters and spaces only (2+ chars).
     */
    public static boolean isValidName(String name) {
        if (isEmpty(name)) return false;
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.matches("^[A-Za-z ]+$");
    }

    /**
     * Requirement: at least 8 characters, 1 digit, and 1 symbol.
     */
    public static boolean isValidPassword(String password) {
        if (isEmpty(password)) return false;
        return password.length() >= 8
                && password.matches(".*\\d.*")
                && password.matches(".*[^A-Za-z0-9].*");
    }

    /**
     * @param value numeric string
     * @return {@code true} if value parses to a number greater than 0
     */
    public static boolean isPositiveNumber(String value) {
        try {
            return Double.parseDouble(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Trims a string, converting null to empty string.
     *
     * @param value input string
     * @return trimmed value or empty string
     */
    public static String sanitize(String value) {
        return (value == null) ? "" : value.trim();
    }
}
