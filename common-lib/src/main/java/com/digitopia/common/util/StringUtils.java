package com.digitopia.common.util;

import java.text.Normalizer;

public final class StringUtils {

    private StringUtils() {}

    /**
     * Normalize text to lowercase ASCII-only alphanumeric
     * Used for: Full Name, Organization Name (for searching)
     * Example: "Şirket Adı 123" -> "sirket adi 123"
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
     * Sanitize free text fields (like invitation messages)
     * Removes HTML tags and prevents XSS attacks
     * Example: "<script>alert('xss')</script>" -> ""
     */
    public static String sanitize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String cleaned = text.replaceAll("<[^>]*>", "");

        return cleaned.trim();
    }

    /**
     * Normalize email to lowercase (case-insensitive comparison)
     * Example: "User@Example.COM" -> "user@example.com"
     */
    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        return email.toLowerCase().trim();
    }
}
