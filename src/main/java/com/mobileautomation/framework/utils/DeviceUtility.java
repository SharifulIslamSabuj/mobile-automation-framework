package com.mobileautomation.framework.utils;

import com.mobileautomation.framework.driver.DriverProvider;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.appmanagement.ApplicationState;
import io.appium.java_client.remote.SupportsRotation;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ScreenOrientation;

/**
 * Reusable, generic device operations — no application-specific logic. The
 * driver is obtained fresh from {@link DriverProvider} on every call, never
 * cached.
 */
public final class DeviceUtility {

    private DeviceUtility() {
    }

    public static ScreenOrientation getOrientation() {
        return ((SupportsRotation) DriverProvider.getDriver()).getOrientation();
    }

    public static void rotate(ScreenOrientation orientation) {
        ((SupportsRotation) DriverProvider.getDriver()).rotate(orientation);
    }

    public static Dimension getScreenSize() {
        return DriverProvider.getDriver().manage().window().getSize();
    }

    public static String getCurrentActivity() {
        return androidDriver().currentActivity();
    }

    public static String getCurrentPackage() {
        return androidDriver().getCurrentPackage();
    }

    /** @param appId the app's package identifier (e.g. {@code com.saucelabs.mydemoapp.android}) */
    public static ApplicationState getAppState(String appId) {
        return androidDriver().queryAppState(appId);
    }

    private static AndroidDriver androidDriver() {
        return (AndroidDriver) DriverProvider.getDriver();
    }
}
