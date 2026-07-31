package com.mobileautomation.framework.utils;

import com.mobileautomation.framework.driver.DriverProvider;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;

/**
 * Soft-keyboard operations. The driver is obtained fresh from
 * {@link DriverProvider} on every call, never cached.
 */
public final class KeyboardUtility {

    private KeyboardUtility() {
    }

    public static void hideKeyboard() {
        androidDriver().hideKeyboard();
    }

    public static boolean isKeyboardShown() {
        return androidDriver().isKeyboardShown();
    }

    /**
     * There is no platform-level "show keyboard" command independent of a
     * target field — a soft keyboard only appears once an editable element
     * gains focus. This method is still generic (it works for any editable
     * element passed to it, not a specific screen's field): it taps the
     * given element only if the keyboard is not already shown.
     */
    public static void showKeyboard(WebElement editableElement) {
        if (!isKeyboardShown()) {
            editableElement.click();
        }
    }

    private static AndroidDriver androidDriver() {
        return (AndroidDriver) DriverProvider.getDriver();
    }
}
