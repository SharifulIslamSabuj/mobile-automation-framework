package com.mobileautomation.framework.exceptions;

/** Thrown by {@code core.ElementActions} when a Selenium/Appium element interaction fails, preserving the original cause. */
public class ElementActionException extends FrameworkException {

    public ElementActionException(String message) {
        super(message);
    }

    public ElementActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
