package com.mobileautomation.framework.exceptions;

/**
 * Thrown when the Reporting Foundation cannot initialize, retrieve, or
 * flush a report — e.g. {@code ReportProvider.getTest()} is called before
 * {@code createTest(String)} on the current thread, or the report file
 * cannot be created on disk.
 */
public class ReportingException extends FrameworkException {

    public ReportingException(String message) {
        super(message);
    }

    public ReportingException(String message, Throwable cause) {
        super(message, cause);
    }
}
