package com.mobileautomation.framework.driver;

import io.appium.java_client.AppiumDriver;

/**
 * Extension point for creating a fully-initialized {@link AppiumDriver}
 * session. {@link AndroidDriverFactory} is the only implementation today
 * (Android + UiAutomator2, per the frozen stack). A future cloud-vendor or
 * additional-platform integration implements this same interface — neither
 * {@link DriverManager} nor {@link DriverProvider} depend on the concrete
 * implementation, only on this interface, so they require no changes when a
 * new {@link DriverFactory} is introduced.
 */
public interface DriverFactory {

    /**
     * Creates and returns a new, ready-to-use driver session.
     *
     * @throws com.mobileautomation.framework.exceptions.DriverInitializationException if the session cannot be created
     */
    AppiumDriver createDriver();
}
