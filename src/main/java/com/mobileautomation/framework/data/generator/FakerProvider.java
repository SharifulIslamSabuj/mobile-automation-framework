package com.mobileautomation.framework.data.generator;

import net.datafaker.Faker;

import java.util.Random;

/**
 * The single point of {@link Faker} instantiation for the whole framework —
 * no other class may call {@code new Faker(...)} directly. Centralizing
 * this makes deterministic-seed testing possible without hunting down every
 * call site.
 */
public final class FakerProvider {

    private static volatile Faker instance;

    private FakerProvider() {
    }

    /** The shared, non-deterministic {@link Faker} instance (double-checked-locking singleton, one per JVM). */
    public static Faker get() {
        Faker result = instance;
        if (result == null) {
            synchronized (FakerProvider.class) {
                result = instance;
                if (result == null) {
                    instance = result = new Faker();
                }
            }
        }
        return result;
    }

    /** A fresh, independently-seeded {@link Faker} for a call site that needs reproducible generated values (e.g. a data-consistency check across two loads). Does not affect {@link #get()}'s shared instance. */
    public static Faker withSeed(long seed) {
        return new Faker(new Random(seed));
    }
}
