package com.mobileautomation.framework.data.provider;

import java.util.List;

/**
 * Reusable, generic invalid/boundary datasets for validation testing — data
 * only, no automation scenario or assertion logic. Every list is a fixed,
 * curated set (not randomly generated — see {@code data.generator} for
 * random values instead).
 */
public final class NegativeDataProvider {

    private NegativeDataProvider() {
    }

    public static String emptyString() {
        return "";
    }

    /** Returns {@code null} explicitly — named for readability at call sites (e.g. {@code verifyRejected(NegativeDataProvider.nullValue())}) rather than a bare {@code null} literal. */
    public static String nullValue() {
        return null;
    }

    public static List<String> invalidEmails() {
        return List.of(
                "plainaddress",
                "@missing-local-part.com",
                "missing-at-sign.com",
                "two@@at-signs.com",
                "trailing-dot@example.com.",
                "space in@example.com"
        );
    }

    /** @return the empty string, a single character, a string exactly {@code maxLength} long, and one character over {@code maxLength} — the four values every boundary check should exercise. */
    public static List<String> boundaryValueStrings(int maxLength) {
        return List.of("", "a", "a".repeat(maxLength), "a".repeat(maxLength + 1));
    }

    public static String oversizedText(int length) {
        return "a".repeat(length);
    }

    public static List<String> specialCharacters() {
        return List.of("!@#$%^&*()", "<>?:\"{}|~`", "\t\n\r", "😀🚀💥", "日本語テスト");
    }

    public static List<String> sqlInjectionStrings() {
        return List.of(
                "' OR '1'='1",
                "'; DROP TABLE users; --",
                "1; SELECT * FROM information_schema.tables",
                "' UNION SELECT NULL--",
                "admin'--"
        );
    }

    public static List<String> xssPayloads() {
        return List.of(
                "<script>alert('xss')</script>",
                "\"><img src=x onerror=alert(1)>",
                "javascript:alert(1)",
                "<svg/onload=alert(1)>",
                "'-alert(1)-'"
        );
    }
}
