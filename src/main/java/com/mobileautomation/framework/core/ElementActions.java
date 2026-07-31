package com.mobileautomation.framework.core;

import com.mobileautomation.framework.exceptions.ElementActionException;
import com.mobileautomation.framework.logging.LogManager;
import com.mobileautomation.framework.reporting.ScreenshotManager;
import com.mobileautomation.framework.utils.WaitUtility;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Reusable abstraction over Selenium/Appium element interactions, built
 * entirely on {@link WaitUtility} (never a raw {@code findElement} call, and
 * never {@code Thread.sleep()} per MA-FR-001 §5). Locators are always
 * supplied by the caller — this class hardcodes no application-specific
 * locator. On failure, every state-changing or state-reading method logs
 * the failure, captures a screenshot via {@link ScreenshotManager}, and
 * throws {@link ElementActionException} with the original cause preserved —
 * it never swallows an exception.
 * <p>
 * Stateless and thread-safe: holds no fields, obtains the driver only
 * indirectly through {@link WaitUtility} on every call.
 */
public class ElementActions {

    private static final Logger LOGGER = LogManager.getLogger(ElementActions.class);

    public WebElement click(By locator) {
        return performOnClickable(locator, "click", WebElement::click);
    }

    public WebElement type(By locator, String text) {
        return performOnVisible(locator, "type", element -> element.sendKeys(text));
    }

    public WebElement clear(By locator) {
        return performOnVisible(locator, "clear", WebElement::clear);
    }

    public String getText(By locator) {
        return supplyFromVisible(locator, "getText", WebElement::getText);
    }

    /** @return {@code true} if the element becomes visible within the configured explicit-wait timeout, {@code false} on timeout (not treated as a failure). */
    public boolean isDisplayed(By locator) {
        try {
            WaitUtility.waitForVisibility(locator);
            return true;
        } catch (TimeoutException e) {
            LOGGER.debug("Element not displayed within timeout: {}", locator);
            return false;
        }
    }

    public boolean isEnabled(By locator) {
        return supplyFromPresent(locator, "isEnabled", WebElement::isEnabled);
    }

    public boolean isSelected(By locator) {
        return supplyFromPresent(locator, "isSelected", WebElement::isSelected);
    }

    /** Ensures the element ends up selected (e.g. a checkbox/toggle) — clicks only if not already selected. */
    public WebElement select(By locator) {
        return performOnClickable(locator, "select", element -> {
            if (!element.isSelected()) {
                element.click();
            }
        });
    }

    /** Ensures the element ends up deselected (e.g. a checkbox/toggle) — clicks only if currently selected. */
    public WebElement deselect(By locator) {
        return performOnClickable(locator, "deselect", element -> {
            if (element.isSelected()) {
                element.click();
            }
        });
    }

    // ---- internal plumbing -------------------------------------------------

    private WebElement performOnClickable(By locator, String actionName, Consumer<WebElement> action) {
        return execute(locator, actionName, () -> {
            WebElement element = WaitUtility.waitForClickable(locator);
            action.accept(element);
            return element;
        });
    }

    private WebElement performOnVisible(By locator, String actionName, Consumer<WebElement> action) {
        return execute(locator, actionName, () -> {
            WebElement element = WaitUtility.waitForVisibility(locator);
            action.accept(element);
            return element;
        });
    }

    private <T> T supplyFromVisible(By locator, String actionName, Function<WebElement, T> supplier) {
        return execute(locator, actionName, () -> supplier.apply(WaitUtility.waitForVisibility(locator)));
    }

    private <T> T supplyFromPresent(By locator, String actionName, Function<WebElement, T> supplier) {
        return execute(locator, actionName, () -> supplier.apply(WaitUtility.waitForPresence(locator)));
    }

    private <T> T execute(By locator, String actionName, java.util.function.Supplier<T> action) {
        try {
            T result = action.get();
            LOGGER.info("Performed '{}' on element: {}", actionName, locator);
            return result;
        } catch (RuntimeException e) {
            LOGGER.error("Element action '{}' failed on locator {}: {}", actionName, locator, e.getMessage(), e);
            ScreenshotManager.captureScreenshot("element_action_" + actionName + "_failure");
            throw new ElementActionException(
                    "Element action '" + actionName + "' failed on locator: " + locator, e);
        }
    }
}
