package com.mobileautomation.framework.data.generator;

import com.mobileautomation.framework.utils.DateUtility;

/**
 * Reusable runtime-context generators — values that need to be fresh/unique
 * for a given test execution, not just random. Composes
 * {@link RandomDataGenerator} (itself built on {@code utils.RandomUtility}),
 * {@code utils.DateUtility}, and {@link FakerProvider} rather than
 * duplicating any of their logic, matching the dependency shape
 * Dynamic Data → Random/Date/Faker → Factories.
 */
public final class DynamicDataGenerator {

    private static final String EMAIL_DOMAIN = "@example-test.com";

    private DynamicDataGenerator() {
    }

    /** A username unique to this call, timestamp-suffixed so parallel executions never collide. */
    public static String uniqueUsername() {
        return RandomDataGenerator.randomUsername() + "_" + DateUtility.timestampForFilename();
    }

    /** An email unique to this call, timestamp-suffixed so parallel executions never collide. */
    public static String uniqueEmail() {
        return RandomDataGenerator.randomUsername() + "_" + DateUtility.timestampForFilename() + EMAIL_DOMAIN;
    }

    public static String phoneNumber() {
        return FakerProvider.get().phoneNumber().phoneNumber();
    }

    public static String timestamp() {
        return DateUtility.timestampForFilename();
    }

    public static String uuid() {
        return RandomDataGenerator.randomUuid();
    }

    /** A value suitable for a throwaway per-test session identifier — combines a UUID with a timestamp for both uniqueness and readability in logs/reports. */
    public static String sessionValue() {
        return uuid() + "_" + timestamp();
    }
}
