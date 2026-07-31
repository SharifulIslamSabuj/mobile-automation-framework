package com.mobileautomation.framework.exceptions;

/**
 * Thrown when a Core Utilities operation fails in a way that needs
 * additional context beyond the underlying exception (e.g., a file
 * operation naming the path involved). Not used to re-wrap Selenium/Appium
 * exceptions (e.g. {@code TimeoutException}, {@code NoSuchElementException})
 * — those are already meaningful on their own and are left to propagate
 * as-is, per the selective-wrapping decision recorded in
 * docs/framework/CORE_UTILITIES_ARCHITECTURE.md.
 */
public class UtilityOperationException extends FrameworkException {

    public UtilityOperationException(String message) {
        super(message);
    }

    public UtilityOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
