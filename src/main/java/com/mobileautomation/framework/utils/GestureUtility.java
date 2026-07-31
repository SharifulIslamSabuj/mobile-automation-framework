package com.mobileautomation.framework.utils;

import com.mobileautomation.framework.driver.DriverProvider;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.List;

import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

/**
 * Reusable mobile gestures built on the W3C Actions API only
 * ({@link PointerInput}/{@link Sequence}) — the deprecated Appium
 * {@code TouchAction}/{@code MultiTouchAction} classes (removed from modern
 * Appium Java Client best practice) are never used. The driver is obtained
 * fresh from {@link DriverProvider} on every call, never cached.
 */
public final class GestureUtility {

    private static final Duration TAP_DURATION = Duration.ofMillis(100);
    private static final Duration LONG_PRESS_DEFAULT_DURATION = Duration.ofSeconds(1);
    private static final Duration SWIPE_DEFAULT_DURATION = Duration.ofMillis(400);
    private static final Duration FLING_DURATION = Duration.ofMillis(120);

    private GestureUtility() {
    }

    public static void tap(WebElement element) {
        Point center = centerOf(element);
        tap(center.getX(), center.getY());
    }

    public static void tap(int x, int y) {
        PointerInput finger = new PointerInput(TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 0)
                .addAction(finger.createPointerMove(Duration.ZERO, viewport(), x, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(TAP_DURATION, viewport(), x, y))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        perform(sequence);
    }

    public static void doubleTap(WebElement element) {
        Point center = centerOf(element);
        doubleTap(center.getX(), center.getY());
    }

    /**
     * Performs two discrete {@link #tap(int, int)} calls. Deliberately does
     * not insert an artificial delay between them (no {@code Thread.sleep()},
     * per MA-FR-001 §5) — the network round-trip and server-side processing
     * time each {@code tap()} call already takes against a real Appium
     * session is sufficient for UiAutomator2 to register two separate taps
     * rather than requiring a synthesized gap.
     */
    public static void doubleTap(int x, int y) {
        tap(x, y);
        tap(x, y);
    }

    public static void longPress(WebElement element) {
        longPress(element, LONG_PRESS_DEFAULT_DURATION);
    }

    public static void longPress(WebElement element, Duration duration) {
        Point center = centerOf(element);
        longPress(center.getX(), center.getY(), duration);
    }

    public static void longPress(int x, int y, Duration duration) {
        PointerInput finger = new PointerInput(TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 0)
                .addAction(finger.createPointerMove(Duration.ZERO, viewport(), x, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(duration, viewport(), x, y))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        perform(sequence);
    }

    public static void swipe(int startX, int startY, int endX, int endY) {
        swipe(startX, startY, endX, endY, SWIPE_DEFAULT_DURATION);
    }

    public static void swipe(int startX, int startY, int endX, int endY, Duration duration) {
        performLinearGesture(startX, startY, endX, endY, duration);
    }

    public static void dragAndDrop(WebElement source, WebElement target) {
        Point from = centerOf(source);
        Point to = centerOf(target);
        dragAndDrop(from.getX(), from.getY(), to.getX(), to.getY());
    }

    public static void dragAndDrop(int startX, int startY, int endX, int endY) {
        // A drag is a swipe with a deliberate hold at the start so the OS
        // recognizes it as a drag rather than a flick.
        PointerInput finger = new PointerInput(TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 0)
                .addAction(finger.createPointerMove(Duration.ZERO, viewport(), startX, startY))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(LONG_PRESS_DEFAULT_DURATION, viewport(), startX, startY))
                .addAction(finger.createPointerMove(SWIPE_DEFAULT_DURATION, viewport(), endX, endY))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        perform(sequence);
    }

    /** A fast, short-duration swipe — distinct from {@link #swipe} by timing only, matching how a "fling" differs from a deliberate swipe on Android. */
    public static void fling(int startX, int startY, int endX, int endY) {
        performLinearGesture(startX, startY, endX, endY, FLING_DURATION);
    }

    private static void performLinearGesture(int startX, int startY, int endX, int endY, Duration duration) {
        PointerInput finger = new PointerInput(TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 0)
                .addAction(finger.createPointerMove(Duration.ZERO, viewport(), startX, startY))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(duration, viewport(), endX, endY))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        perform(sequence);
    }

    private static void perform(Sequence sequence) {
        ((Interactive) DriverProvider.getDriver()).perform(List.of(sequence));
    }

    private static Point centerOf(WebElement element) {
        Rectangle rect = element.getRect();
        return new Point(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);
    }
}
