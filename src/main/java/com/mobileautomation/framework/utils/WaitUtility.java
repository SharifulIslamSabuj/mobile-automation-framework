package com.mobileautomation.framework.utils;

import com.mobileautomation.framework.config.ConfigReader;
import com.mobileautomation.framework.driver.DriverProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * Explicit-wait operations only — per MA-FR-001 §5, {@code Thread.sleep()}
 * is never used here (or anywhere in the framework) and this class never
 * configures an implicit wait. The driver is obtained fresh from
 * {@link DriverProvider} on every call, never cached as a field, so this
 * class stays stateless and safe under parallel execution. The default
 * timeout is read from the Configuration Layer
 * ({@link ConfigReader#getExplicitWaitTimeout()}) — never a hardcoded
 * literal — and every method has an overload accepting an explicit
 * {@link Duration} for the rare case a step genuinely needs a different one.
 */
public final class WaitUtility {

    private WaitUtility() {
    }

    private static WebDriverWait wait(Duration timeout) {
        return new WebDriverWait(DriverProvider.getDriver(), timeout);
    }

    private static Duration defaultTimeout() {
        return ConfigReader.getInstance().getExplicitWaitTimeout();
    }

    // ---- Visibility ----

    public static WebElement waitForVisibility(WebElement element) {
        return waitForVisibility(element, defaultTimeout());
    }

    public static WebElement waitForVisibility(WebElement element, Duration timeout) {
        return wait(timeout).until(ExpectedConditions.visibilityOf(element));
    }

    public static WebElement waitForVisibility(By locator) {
        return waitForVisibility(locator, defaultTimeout());
    }

    public static WebElement waitForVisibility(By locator, Duration timeout) {
        return wait(timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // ---- Presence ----

    public static WebElement waitForPresence(By locator) {
        return waitForPresence(locator, defaultTimeout());
    }

    public static WebElement waitForPresence(By locator, Duration timeout) {
        return wait(timeout).until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    // ---- Clickable ----

    public static WebElement waitForClickable(WebElement element) {
        return waitForClickable(element, defaultTimeout());
    }

    public static WebElement waitForClickable(WebElement element, Duration timeout) {
        return wait(timeout).until(ExpectedConditions.elementToBeClickable(element));
    }

    public static WebElement waitForClickable(By locator) {
        return waitForClickable(locator, defaultTimeout());
    }

    public static WebElement waitForClickable(By locator, Duration timeout) {
        return wait(timeout).until(ExpectedConditions.elementToBeClickable(locator));
    }

    // ---- Invisibility ----

    public static boolean waitForInvisibility(WebElement element) {
        return waitForInvisibility(element, defaultTimeout());
    }

    public static boolean waitForInvisibility(WebElement element, Duration timeout) {
        return wait(timeout).until(ExpectedConditions.invisibilityOf(element));
    }

    public static boolean waitForInvisibility(By locator) {
        return waitForInvisibility(locator, defaultTimeout());
    }

    public static boolean waitForInvisibility(By locator, Duration timeout) {
        return wait(timeout).until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // ---- Attribute conditions ----

    public static boolean waitForAttributeContains(WebElement element, String attribute, String value) {
        return waitForAttributeContains(element, attribute, value, defaultTimeout());
    }

    public static boolean waitForAttributeContains(WebElement element, String attribute, String value, Duration timeout) {
        return wait(timeout).until(ExpectedConditions.attributeContains(element, attribute, value));
    }

    public static boolean waitForAttributeToBe(WebElement element, String attribute, String value) {
        return waitForAttributeToBe(element, attribute, value, defaultTimeout());
    }

    public static boolean waitForAttributeToBe(WebElement element, String attribute, String value, Duration timeout) {
        return wait(timeout).until(ExpectedConditions.attributeToBe(element, attribute, value));
    }

    // ---- Elements present (e.g. RecyclerView children) ----

    public static List<WebElement> waitForAllPresence(By locator) {
        return waitForAllPresence(locator, defaultTimeout());
    }

    public static List<WebElement> waitForAllPresence(By locator, Duration timeout) {
        return wait(timeout).until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    // ---- Custom wait support (the escape hatch every other method is built on) ----

    public static <T> T waitUntil(Function<WebDriver, T> condition) {
        return waitUntil(condition, defaultTimeout());
    }

    public static <T> T waitUntil(Function<WebDriver, T> condition, Duration timeout) {
        return wait(timeout).until(condition);
    }
}
