package com.mobileautomation.framework.utils;

import com.mobileautomation.framework.driver.DriverProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Android Toast detection. Deliberately returns {@link Optional}/{@code boolean}
 * rather than throwing on "not found" — this class only detects and reads,
 * it never asserts (assertions belong to the Test Layer, per MA-FR-001 §3).
 * The driver is obtained fresh from {@link DriverProvider} on every call.
 */
public final class ToastUtility {

    private static final By TOAST_LOCATOR = By.className("android.widget.Toast");

    private ToastUtility() {
    }

    /** @return {@code true} if a toast is on screen right now (no wait). */
    public static boolean isToastDisplayed() {
        return !DriverProvider.getDriver().findElements(TOAST_LOCATOR).isEmpty();
    }

    /** @return the currently-displayed toast's text, or empty if none is on screen right now (no wait). */
    public static Optional<String> getToastText() {
        List<WebElement> toasts = DriverProvider.getDriver().findElements(TOAST_LOCATOR);
        return toasts.isEmpty() ? Optional.empty() : Optional.of(toasts.get(0).getText());
    }

    /**
     * Waits (explicit wait only) for a toast to appear and returns its text,
     * or empty if none appeared within the timeout — a timeout is treated as
     * a normal, non-exceptional outcome here, not a failure to propagate.
     */
    public static Optional<String> waitForToast(Duration timeout) {
        try {
            WebElement toast = WaitUtility.waitForPresence(TOAST_LOCATOR, timeout);
            return Optional.of(toast.getText());
        } catch (TimeoutException e) {
            return Optional.empty();
        }
    }
}
