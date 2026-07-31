package com.mobileautomation.framework.logging;

import com.mobileautomation.framework.config.ConfigReader;
import com.mobileautomation.framework.constants.ConfigurationKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The single entry point every framework class must use to obtain an
 * SLF4J {@link Logger} — never {@link org.slf4j.LoggerFactory} directly, and
 * never a Log4j2 class directly (per MA-CS-001 §11). Beyond being a thin
 * factory, this class bridges the Configuration Layer's log level and log
 * directory into JVM system properties that {@code log4j2.xml} reads via
 * {@code ${sys:...}} lookups — and it does so <b>before</b> the first
 * logger is created, which is the only point at which Log4j2's own context
 * (and therefore its appender file paths and root level) is still
 * unconfigured. Bypassing this class and calling
 * {@code org.slf4j.LoggerFactory.getLogger(...)} directly would skip that
 * bridge and silently fall back to log4j2.xml's hardcoded defaults
 * (level {@code INFO}, directory {@code logs}) regardless of what the
 * Configuration Layer actually says.
 */
public final class LogManager {

    private static volatile boolean systemPropertiesBridged = false;

    private LogManager() {
    }

    public static Logger getLogger(Class<?> clazz) {
        ensureConfigurationBridged();
        return LoggerFactory.getLogger(clazz);
    }

    private static void ensureConfigurationBridged() {
        if (systemPropertiesBridged) {
            return;
        }
        synchronized (LogManager.class) {
            if (systemPropertiesBridged) {
                return;
            }
            ConfigReader configReader = ConfigReader.getInstance();
            setIfAbsent(ConfigurationKeys.LOG_LEVEL, configReader.getLogLevel());
            setIfAbsent(ConfigurationKeys.LOGGING_DIRECTORY, configReader.getLoggingDirectory());
            systemPropertiesBridged = true;
        }
    }

    /** Never overwrites a value the caller already supplied directly as a JVM system property (e.g. {@code -Dlog.level=DEBUG}). */
    private static void setIfAbsent(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }
}
