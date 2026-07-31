package com.mobileautomation.framework.utils;

import com.mobileautomation.framework.driver.DriverProvider;
import io.appium.java_client.android.AndroidDriver;

import java.time.Duration;

/**
 * Reusable, generic application-lifecycle operations — no navigation logic
 * (moving between screens within the app is a Page Object concern, not a
 * utility concern). The driver is obtained fresh from {@link DriverProvider}
 * on every call, never cached.
 * <p>
 * Modern Appium's Android API exposes exactly three distinct app-lifecycle
 * primitives — {@code activateApp}, {@code terminateApp}, and
 * {@code runAppInBackground} — rather than five. {@link #launchApp(String)}
 * and {@link #closeApp(String)} are provided as clearly-named aliases for
 * discoverability (matching this phase's requested vocabulary) but
 * deliberately delegate to {@link #activateApp(String)}/{@link #terminateApp(String)}
 * rather than duplicating logic.
 */
public final class AppUtility {

    private AppUtility() {
    }

    /** Alias for {@link #activateApp(String)}. */
    public static void launchApp(String appId) {
        activateApp(appId);
    }

    /** Brings the given app to the foreground, launching it first if it is not already running. */
    public static void activateApp(String appId) {
        androidDriver().activateApp(appId);
    }

    /** Alias for {@link #terminateApp(String)}. */
    public static boolean closeApp(String appId) {
        return terminateApp(appId);
    }

    /** @return {@code true} if the app was running and was terminated, {@code false} if it was not running. */
    public static boolean terminateApp(String appId) {
        return androidDriver().terminateApp(appId);
    }

    /** Sends the current app to the background for the given duration (or indefinitely, per Appium's semantics, if a negative duration is supplied). */
    public static void backgroundApp(Duration duration) {
        androidDriver().runAppInBackground(duration);
    }

    private static AndroidDriver androidDriver() {
        return (AndroidDriver) DriverProvider.getDriver();
    }
}
