package com.mobileautomation.framework.utils;

import com.mobileautomation.framework.driver.DriverProvider;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;

/**
 * Android {@code UiScrollable}-based scrolling. Every method here is
 * generic (a target text/resource-id/accessibility-id is a parameter, never
 * baked in) — no application-specific scrolling logic lives in this class.
 * The driver is obtained fresh from {@link DriverProvider} on every call,
 * never cached.
 */
public final class ScrollUtility {

    private static final String SCROLLABLE_VERTICAL =
            "new UiScrollable(new UiSelector().scrollable(true))";
    private static final String SCROLLABLE_HORIZONTAL =
            "new UiScrollable(new UiSelector().scrollable(true)).setAsHorizontalList()";

    private ScrollUtility() {
    }

    /** Scrolls one page forward (down) within the nearest scrollable container. */
    public static void scrollDown() {
        DriverProvider.getDriver().findElement(AppiumBy.androidUIAutomator(SCROLLABLE_VERTICAL + ".scrollForward();"));
    }

    /** Scrolls one page backward (up) within the nearest scrollable container. */
    public static void scrollUp() {
        DriverProvider.getDriver().findElement(AppiumBy.androidUIAutomator(SCROLLABLE_VERTICAL + ".scrollBackward();"));
    }

    /** Scrolls one page forward (right) within the nearest horizontally-scrollable container. */
    public static void scrollRight() {
        DriverProvider.getDriver().findElement(AppiumBy.androidUIAutomator(SCROLLABLE_HORIZONTAL + ".scrollForward();"));
    }

    /** Scrolls one page backward (left) within the nearest horizontally-scrollable container. */
    public static void scrollLeft() {
        DriverProvider.getDriver().findElement(AppiumBy.androidUIAutomator(SCROLLABLE_HORIZONTAL + ".scrollBackward();"));
    }

    /** Scrolls (vertically) until an element with the exact given text is visible, and returns it. */
    public static WebElement scrollToText(String text) {
        String expression = SCROLLABLE_VERTICAL + ".scrollIntoView(new UiSelector().text(" + quote(text) + "));";
        return DriverProvider.getDriver().findElement(AppiumBy.androidUIAutomator(expression));
    }

    /** Scrolls (vertically) until an element whose text contains the given substring is visible, and returns it. */
    public static WebElement scrollToTextContains(String text) {
        String expression = SCROLLABLE_VERTICAL + ".scrollIntoView(new UiSelector().textContains(" + quote(text) + "));";
        return DriverProvider.getDriver().findElement(AppiumBy.androidUIAutomator(expression));
    }

    /** Scrolls (vertically) until an element with the given resource-id is visible, and returns it. */
    public static WebElement scrollToResourceId(String resourceId) {
        String expression = SCROLLABLE_VERTICAL + ".scrollIntoView(new UiSelector().resourceId(" + quote(resourceId) + "));";
        return DriverProvider.getDriver().findElement(AppiumBy.androidUIAutomator(expression));
    }

    /** Scrolls (vertically) until an element with the given accessibility id (content-description) is visible, and returns it. */
    public static WebElement scrollToAccessibilityId(String accessibilityId) {
        String expression = SCROLLABLE_VERTICAL + ".scrollIntoView(new UiSelector().description(" + quote(accessibilityId) + "));";
        return DriverProvider.getDriver().findElement(AppiumBy.androidUIAutomator(expression));
    }

    /**
     * Escapes double quotes in a value destined for a UiAutomator2 expression
     * string, then wraps it in double quotes. Prevents a locator value
     * containing a quote character from breaking the generated expression.
     */
    private static String quote(String value) {
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }
}
