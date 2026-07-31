package com.mobileautomation.framework.exceptions;

/** Thrown by the {@code data} package's readers/loader for a missing or malformed test-data resource, preserving the original cause. */
public class TestDataException extends FrameworkException {

    public TestDataException(String message) {
        super(message);
    }

    public TestDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
