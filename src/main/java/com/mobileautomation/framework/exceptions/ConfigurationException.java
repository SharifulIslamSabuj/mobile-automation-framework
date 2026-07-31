package com.mobileautomation.framework.exceptions;

/**
 * Thrown when the Configuration Layer cannot load, resolve, or parse a
 * required configuration value. Unchecked: a configuration failure is a
 * fail-fast framework startup error, not a recoverable per-test condition.
 */
public class ConfigurationException extends FrameworkException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
