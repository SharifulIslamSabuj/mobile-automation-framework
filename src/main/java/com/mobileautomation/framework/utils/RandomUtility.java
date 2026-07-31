package com.mobileautomation.framework.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Reusable random-value generation for framework/test mechanics (e.g.,
 * uniqueness suffixes for file names) — never business/domain test data
 * (that belongs to the Test Data layer, a later phase, per MA-TDD-001).
 * Stateless and thread-safe: uses {@link ThreadLocalRandom}, not a single
 * shared {@code java.util.Random} instance, so parallel test execution never
 * contends on one random source.
 */
public final class RandomUtility {

    private RandomUtility() {
    }

    /** Inclusive of both {@code min} and {@code max}. */
    public static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static String randomAlphabetic(int length) {
        return RandomStringUtils.insecure().nextAlphabetic(length);
    }

    public static String randomAlphanumeric(int length) {
        return RandomStringUtils.insecure().nextAlphanumeric(length);
    }

    public static String randomUuid() {
        return UUID.randomUUID().toString();
    }

    /** A filesystem-safe "now" timestamp, for building unique names — delegates to {@link DateUtility} rather than duplicating formatting logic. */
    public static String timestampSuffix() {
        return DateUtility.timestampForFilename();
    }
}
