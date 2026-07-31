package com.mobileautomation.framework.exceptions;

/** Thrown by {@code data.validator.DataValidator} when a loaded dataset fails a required-field/null check. */
public class DataValidationException extends FrameworkException {

    public DataValidationException(String message) {
        super(message);
    }

    public DataValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
