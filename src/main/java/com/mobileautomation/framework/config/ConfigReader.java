package com.mobileautomation.framework.config;

import com.mobileautomation.framework.constants.ConfigurationDefaults;
import com.mobileautomation.framework.constants.ConfigurationKeys;
import com.mobileautomation.framework.exceptions.ConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * Single source of truth for every configurable value in the framework.
 * <p>
 * Loading order (lowest to highest precedence): the common
 * {@code config.properties} file, then the active {@link Environment}'s
 * {@code config-<profileName>.properties} file (its keys override the
 * common file's), then any matching JVM system property supplied at
 * execution time (highest precedence, e.g. {@code -Dapp.path=...}).
 * <p>
 * No other class should read a {@code .properties} file or reference a
 * configuration key string directly — every value is obtained through this
 * class's typed accessors.
 */
public final class ConfigReader {

    private static volatile ConfigReader instance;

    private final Properties properties = new Properties();
    private final Environment environment;

    private ConfigReader() {
        this.environment = resolveEnvironment();
        loadPropertiesFile(ConfigurationDefaults.CONFIG_FILE_PREFIX + ConfigurationDefaults.CONFIG_FILE_EXTENSION);
        loadPropertiesFile(ConfigurationDefaults.CONFIG_FILE_PREFIX + "-" + environment.getProfileName()
                + ConfigurationDefaults.CONFIG_FILE_EXTENSION);
    }

    /**
     * Returns the shared {@link ConfigReader} instance, loading configuration
     * on first access. Thread-safe (double-checked locking).
     */
    public static ConfigReader getInstance() {
        ConfigReader result = instance;
        if (result == null) {
            synchronized (ConfigReader.class) {
                result = instance;
                if (result == null) {
                    instance = result = new ConfigReader();
                }
            }
        }
        return result;
    }

    private Environment resolveEnvironment() {
        String requested = System.getProperty(ConfigurationKeys.ENVIRONMENT_SYSTEM_PROPERTY);
        if (requested == null || requested.isBlank()) {
            return Environment.fromProfileName(ConfigurationDefaults.DEFAULT_ENVIRONMENT_PROFILE);
        }
        return Environment.fromProfileName(requested);
    }

    private void loadPropertiesFile(String fileName) {
        String resourcePath = ConfigurationDefaults.CONFIG_RESOURCE_DIRECTORY + fileName;
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new ConfigurationException("Configuration file not found on classpath: " + resourcePath);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read configuration file: " + resourcePath, e);
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    // ---- Generic strongly-typed accessors ------------------------------

    /**
     * @throws ConfigurationException if the key is missing/blank in every configuration tier
     */
    public String getRequiredString(String key) {
        String value = getString(key, null);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("Required configuration value is missing or blank for key: '" + key + "'");
        }
        return value;
    }

    public String getString(String key, String defaultValue) {
        String systemOverride = System.getProperty(key);
        if (systemOverride != null && !systemOverride.isBlank()) {
            return systemOverride;
        }
        String value = properties.getProperty(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    /**
     * @throws ConfigurationException if a value is present but not a valid integer
     */
    public int getInt(String key, int defaultValue) {
        String value = getString(key, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Invalid integer value for key '" + key + "': '" + value + "'", e);
        }
    }

    /**
     * @throws ConfigurationException if a value is present but is not exactly "true" or "false" (case-insensitive)
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key, null);
        if (value == null) {
            return defaultValue;
        }
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new ConfigurationException(
                    "Invalid boolean value for key '" + key + "': '" + value + "'. Expected 'true' or 'false'.");
        }
        return Boolean.parseBoolean(value.trim());
    }

    public Duration getDurationSeconds(String key, int defaultSeconds) {
        return Duration.ofSeconds(getInt(key, defaultSeconds));
    }

    // ---- Domain-specific accessors (no magic strings outside this class) ----

    public String getAppiumServerUrl() {
        return getString(ConfigurationKeys.APPIUM_SERVER_URL, ConfigurationDefaults.DEFAULT_APPIUM_SERVER_URL);
    }

    public String getAppPath() {
        return getString(ConfigurationKeys.APP_PATH, "");
    }

    public String getAppPackage() {
        return getRequiredString(ConfigurationKeys.APP_PACKAGE);
    }

    public String getAppActivity() {
        return getRequiredString(ConfigurationKeys.APP_ACTIVITY);
    }

    public String getDeviceName() {
        return getString(ConfigurationKeys.DEVICE_NAME, "");
    }

    public String getPlatformName() {
        return getString(ConfigurationKeys.PLATFORM_NAME, ConfigurationDefaults.DEFAULT_PLATFORM_NAME);
    }

    public String getPlatformVersion() {
        return getString(ConfigurationKeys.PLATFORM_VERSION, "");
    }

    public String getUdid() {
        return getString(ConfigurationKeys.DEVICE_UDID, ConfigurationDefaults.DEFAULT_UDID);
    }

    public String getAutomationName() {
        return getString(ConfigurationKeys.AUTOMATION_NAME, ConfigurationDefaults.DEFAULT_AUTOMATION_NAME);
    }

    public Duration getNewCommandTimeout() {
        return getDurationSeconds(ConfigurationKeys.DRIVER_NEW_COMMAND_TIMEOUT_SECONDS,
                ConfigurationDefaults.DEFAULT_NEW_COMMAND_TIMEOUT_SECONDS);
    }

    /** Per MA-FR-001 §5, this must not be relied upon as the primary synchronization strategy. */
    public Duration getImplicitWaitTimeout() {
        return getDurationSeconds(ConfigurationKeys.DRIVER_IMPLICIT_WAIT_SECONDS,
                ConfigurationDefaults.DEFAULT_IMPLICIT_WAIT_SECONDS);
    }

    public Duration getExplicitWaitTimeout() {
        return getDurationSeconds(ConfigurationKeys.DRIVER_EXPLICIT_WAIT_TIMEOUT_SECONDS,
                ConfigurationDefaults.DEFAULT_EXPLICIT_WAIT_TIMEOUT_SECONDS);
    }

    public Duration getPageLoadTimeout() {
        return getDurationSeconds(ConfigurationKeys.DRIVER_PAGE_LOAD_TIMEOUT_SECONDS,
                ConfigurationDefaults.DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS);
    }

    public ExecutionMode getExecutionMode() {
        return ExecutionMode.fromConfigValue(getString(ConfigurationKeys.EXECUTION_MODE, environment.name()));
    }

    /**
     * The active {@link ExecutionStrategy} — {@link ExecutionStrategy#ISOLATED}
     * unless {@code execution.strategy} is explicitly set otherwise. Added
     * Phase 9.5I.
     */
    public ExecutionStrategy getExecutionStrategy() {
        return ExecutionStrategy.fromConfigValue(
                getString(ConfigurationKeys.EXECUTION_STRATEGY, ConfigurationDefaults.DEFAULT_EXECUTION_STRATEGY));
    }

    /**
     * Derived from {@link #getExecutionStrategy()} as of Phase 9.5I:
     * {@link ExecutionStrategy#FAST} requests {@code noReset=true} (reuse
     * AUT state); {@link ExecutionStrategy#ISOLATED} (the default) requests
     * {@code noReset=false}, so Appium clears the AUT's own data at every
     * session start. No longer an independently-set configuration value —
     * see {@code config.ExecutionStrategy}'s Javadoc for why.
     */
    public boolean isNoReset() {
        return getExecutionStrategy() == ExecutionStrategy.FAST;
    }

    public boolean isFullReset() {
        return getBoolean(ConfigurationKeys.EXECUTION_FULL_RESET, ConfigurationDefaults.DEFAULT_FULL_RESET);
    }

    public boolean isAutoGrantPermissions() {
        return getBoolean(ConfigurationKeys.EXECUTION_AUTO_GRANT_PERMISSIONS,
                ConfigurationDefaults.DEFAULT_AUTO_GRANT_PERMISSIONS);
    }

    public String getReportDirectory() {
        return getString(ConfigurationKeys.REPORT_DIRECTORY, ConfigurationDefaults.DEFAULT_REPORT_DIRECTORY);
    }

    public String getScreenshotDirectory() {
        return getString(ConfigurationKeys.REPORT_SCREENSHOT_DIRECTORY,
                ConfigurationDefaults.DEFAULT_SCREENSHOT_DIRECTORY);
    }

    public String getLogLevel() {
        return getString(ConfigurationKeys.LOG_LEVEL, ConfigurationDefaults.DEFAULT_LOG_LEVEL);
    }

    /** Added Phase 6 — the directory Log4j2's file appender writes to. */
    public String getLoggingDirectory() {
        return getString(ConfigurationKeys.LOGGING_DIRECTORY, ConfigurationDefaults.DEFAULT_LOGGING_DIRECTORY);
    }

    public int getRetryCount() {
        return getInt(ConfigurationKeys.EXECUTION_RETRY_COUNT, ConfigurationDefaults.DEFAULT_RETRY_COUNT);
    }
}
