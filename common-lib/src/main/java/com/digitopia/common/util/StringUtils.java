package com.digitopia.common.util;

import java.text.Normalizer;

public final class StringUtils {

    private StringUtils() {}

    /**
     * Normalizes text to lowercase, ASCII-only alphanumeric format.
     * Converts Turkish/special characters to English equivalents and removes diacritics.
     * Used for creating searchable normalized database fields.
     *
     * @param input text to normalize
     * @return normalized lowercase alphanumeric string, or empty if input is null/blank
     */
    public static String normalizeToAscii(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String result = input.toLowerCase().trim();

        result = result
            .replace("ı", "i")
            .replace("ğ", "g")
            .replace("ü", "u")
            .replace("ş", "s")
            .replace("ö", "o")
            .replace("ç", "c");

        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = result.replaceAll("\\p{M}", "");

        result = result.replaceAll("[^a-z0-9\\s]", "");

        result = result.replaceAll("\\s+", " ").trim();

        return result;
    }

    /**
     * Sanitizes user input by removing HTML tags to prevent XSS attacks.
     *
     * @param text free text input (e.g., invitation messages)
     * @return sanitized text without HTML tags, or empty if input is null/blank
     */
    public static String sanitize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String cleaned = text.replaceAll("<[^>]*>", "");

        return cleaned.trim();
    }

    /**
     * Normalizes email to lowercase for case-insensitive comparison.
     *
     * @param email email address
     * @return lowercase email, or empty if input is null/blank
     */
    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        return email.toLowerCase().trim();
    }
}
