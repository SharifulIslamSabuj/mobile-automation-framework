package com.mobileautomation.framework.exceptions;

/**
 * Common root of every exception this framework defines. Abstract
 * deliberately — a caller always throws (and catches) a specific subtype
 * (e.g. {@link ConfigurationException}, {@link DriverInitializationException}),
 * never this base type directly, so "clear exception hierarchy" means
 * something concrete: every framework-level failure is identifiable by its
 * own type, not lumped into one generic exception.
 * <p>
 * Every subtype preserves the root cause via the {@code (message, cause)}
 * constructor wherever an underlying exception is being wrapped — no
 * framework exception is ever thrown without either an original message
 * that explains what failed, or the original exception attached as the
 * cause (or both).
 */
public abstract class FrameworkException extends RuntimeException {

    protected FrameworkException(String message) {
        super(message);
    }

    protected FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
