package com.mobileautomation.framework.exceptions;

/**
 * Thrown when the Driver layer cannot create, retrieve, or configure an
 * Appium driver session — e.g. the Appium server is unreachable, the
 * configured capabilities are invalid, or {@code getDriver()} is called
 * before {@code initializeDriver()} on the current thread. Unchecked: a
 * driver-lifecycle failure is a fail-fast error, not a recoverable
 * per-step condition.
 */
public class DriverInitializationException extends FrameworkException {

    public DriverInitializationException(String message) {
        super(message);
    }

    public DriverInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
