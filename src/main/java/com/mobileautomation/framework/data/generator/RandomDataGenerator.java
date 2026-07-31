package com.mobileautomation.framework.data.generator;

import com.mobileautomation.framework.utils.RandomUtility;

/** Reusable pure-randomness generators, built on {@code utils.RandomUtility} (Phase 5) rather than duplicating it. */
public final class RandomDataGenerator {

    private static final String EMAIL_DOMAIN = "@example-test.com";

    private RandomDataGenerator() {
    }

    public static int randomNumber(int min, int max) {
        return RandomUtility.randomInt(min, max);
    }

    public static String randomString(int length) {
        return RandomUtility.randomAlphanumeric(length);
    }

    public static String randomUuid() {
        return RandomUtility.randomUuid();
    }

    public static String randomUsername() {
        return "user_" + randomString(8).toLowerCase();
    }

    public static String randomEmail() {
        return randomString(10).toLowerCase() + EMAIL_DOMAIN;
    }
}
