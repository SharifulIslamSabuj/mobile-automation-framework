package com.mobileautomation.framework.config;

import com.mobileautomation.framework.exceptions.ConfigurationException;

/**
 * How/where the current run is orchestrated — an axis distinct from
 * {@link Environment}. {@code Environment} answers "which device-target
 * properties file is loaded"; {@code ExecutionMode} answers "how is the
 * Appium session for this run orchestrated." The two overlap in name for
 * {@code EMULATOR}/{@code REAL_DEVICE} because, today, orchestration mirrors
 * the device target — but {@code LOCAL} has no {@link Environment}
 * counterpart, and future orchestration modes (e.g. a remote/cloud grid)
 * would extend this enum without needing a new {@link Environment}.
 * <p>
 * Read from the {@code execution.mode} configuration key; defaults to the
 * active {@link Environment}'s name if not explicitly set (see
 * {@code ConfigReader#getExecutionMode()}).
 */
public enum ExecutionMode {
    LOCAL,
    EMULATOR,
    REAL_DEVICE;

    /**
     * @throws ConfigurationException if the value is blank or does not match a supported mode
     */
    public static ExecutionMode fromConfigValue(String value) {
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("Execution mode value must not be blank.");
        }
        try {
            return ExecutionMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException(
                    "Unsupported execution mode: '" + value + "'. Supported: LOCAL, EMULATOR, REAL_DEVICE.", e);
        }
    }
}
