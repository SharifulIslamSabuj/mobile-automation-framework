package com.mobileautomation.framework.core;

import com.mobileautomation.framework.exceptions.ElementActionException;
import com.mobileautomation.framework.logging.LogManager;
import com.mobileautomation.framework.reporting.ScreenshotManager;
import com.mobileautomation.framework.utils.WaitUtility;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;

import java.util.List;
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

    /** @return the named attribute (e.g. {@code "content-desc"}) of the element matching {@code locator}. */
    public String getAttribute(By locator, String attributeName) {
        return supplyFromVisible(locator, "getAttribute:" + attributeName, element -> element.getAttribute(attributeName));
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

    /**
     * Resolves every element matching {@code locator} — the list-returning
     * counterpart every other method here lacks, needed wherever a screen
     * repeats the same locator across multiple items (e.g. a RecyclerView
     * card field). Added Phase 9.2 for exactly this reason: MA-LOC-001 §19.1
     * documents that Catalog/Cart/Review-Order item fields repeat identically
     * per row and must be resolved by index, and no existing method here
     * supported that. Callers index into the returned list themselves
     * (standard {@link WebElement} calls — {@code click()}, {@code getText()}
     * — are safe to use directly on an already-resolved element without
     * re-wrapping).
     */
    public List<WebElement> findAll(By locator) {
        return execute(locator, "findAll", () -> WaitUtility.waitForAllPresence(locator));
    }

    /**
     * Resolves a single element matching {@code relativeLocator} within
     * {@code parent}'s subtree — the scoped counterpart to {@link #findAll(By)}.
     * Added Phase 9.5B for parent-child traversal within an already-verified
     * element (e.g. a RecyclerView card's image, found relative to that
     * card's own name element), instead of correlating two independently
     * resolved top-level lists — see {@link WaitUtility#waitForPresence(WebElement, By)}
     * for why.
     */
    public WebElement findWithin(WebElement parent, By relativeLocator) {
        return execute(relativeLocator, "findWithin", () -> WaitUtility.waitForPresence(parent, relativeLocator));
    }

    /**
     * Clicks an already-resolved element (e.g. one returned by
     * {@link #findWithin(WebElement, By)}) — waits for it to become
     * clickable first, exactly like {@link #click(By)} does for a locator,
     * so an already-resolved element is never clicked without the same
     * explicit-wait discipline the rest of this class guarantees. Added
     * Phase 9.5B.
     */
    public WebElement click(WebElement element) {
        return executeOnElement(element, "click", WebElement::click);
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

    /** Mirrors {@link #execute(By, String, java.util.function.Supplier)}'s logging/screenshot/exception-wrapping guarantees for an already-resolved element, which has no {@link By} to log. */
    private WebElement executeOnElement(WebElement element, String actionName, Consumer<WebElement> action) {
        try {
            WebElement clickable = WaitUtility.waitForClickable(element);
            action.accept(clickable);
            LOGGER.info("Performed '{}' on an already-resolved element.", actionName);
            return clickable;
        } catch (RuntimeException e) {
            LOGGER.error("Element action '{}' failed on an already-resolved element: {}", actionName, e.getMessage(), e);
            ScreenshotManager.captureScreenshot("element_action_" + actionName + "_failure");
            throw new ElementActionException(
                    "Element action '" + actionName + "' failed on an already-resolved element", e);
        }
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
