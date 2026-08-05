package com.mobileautomation.framework.config;

import com.mobileautomation.framework.exceptions.ConfigurationException;

/**
 * Governs whether a fresh, clean AUT state is guaranteed before every
 * {@code @Test}, or whether the AUT's own data is allowed to persist across
 * sessions for faster local iteration. An axis distinct from both
 * {@link Environment} (which device-target properties file) and
 * {@link ExecutionMode} (how the Appium session is orchestrated) — this one
 * answers "does application state survive between test methods."
 * <p>
 * Added Phase 9.5I, replacing the previously independent, directly-set
 * {@code execution.noReset} key: driving reset behavior from a single,
 * named strategy avoids the ambiguity of two separately-configurable flags
 * that could otherwise disagree. {@link ConfigReader#isNoReset()} derives
 * its value from this enum.
 * <p>
 * Read from the {@code execution.strategy} configuration key; defaults to
 * {@link #ISOLATED} if not explicitly set — see {@code ConfigReader#getExecutionStrategy()}.
 */
public enum ExecutionStrategy {

    /**
     * Enterprise regression mode — the default. Guarantees no application
     * state (login, cart, selected product, or any other AUT memory)
     * survives from one {@code @Test} to the next: {@code noReset=false} is
     * requested for every session, so Appium clears the AUT's own data at
     * session start without reinstalling the APK ({@code fullReset}
     * remains independently controlled — see {@code ConfigReader#isFullReset()} —
     * and is not implied by this strategy).
     */
    ISOLATED,

    /**
     * Developer/debug mode only — not for regression or CI use. Reuses
     * whatever AUT state already exists ({@code noReset=true}); a new
     * Appium session and a new driver are still created per test exactly as
     * under {@link #ISOLATED} — only the AUT's own data persistence
     * differs. Useful for iterating on a single test locally without
     * repeatedly re-logging-in or re-adding-to-cart by hand.
     */
    FAST;

    /**
     * @throws ConfigurationException if the value is blank or does not match a supported strategy
     */
    public static ExecutionStrategy fromConfigValue(String value) {
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("Execution strategy value must not be blank.");
        }
        try {
            return ExecutionStrategy.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException(
                    "Unsupported execution strategy: '" + value + "'. Supported: ISOLATED, FAST.", e);
        }
    }
}
