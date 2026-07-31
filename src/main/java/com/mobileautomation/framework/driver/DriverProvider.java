package com.mobileautomation.framework.driver;

import io.appium.java_client.AppiumDriver;

/**
 * The single, exclusive entry point every other framework layer (once
 * implemented — Base Framework, Page Objects, Utilities) must use to obtain,
 * initialize, or quit the active driver. No component may reference
 * {@link DriverManager} directly — it is package-private, so the compiler
 * enforces this, not just a naming convention.
 */
public final class DriverProvider {

    private DriverProvider() {
    }

    /** Initializes a driver on the current thread using the default {@link AndroidDriverFactory}. */
    public static void initializeDriver() {
        DriverManager.initializeDriver();
    }

    /** Initializes a driver on the current thread using a caller-supplied {@link DriverFactory}. */
    public static void initializeDriver(DriverFactory driverFactory) {
        DriverManager.initializeDriver(driverFactory);
    }

    /**
     * @throws com.mobileautomation.framework.exceptions.DriverInitializationException if no driver has been initialized on the current thread
     */
    public static AppiumDriver getDriver() {
        return DriverManager.getDriver();
    }

    /** Quits the current thread's driver and cleans up its {@link ThreadLocal} slot. Safe to call even if no driver is active. */
    public static void quitDriver() {
        DriverManager.quitDriver();
    }

    /** @return whether a driver is currently active on this thread. */
    public static boolean hasActiveDriver() {
        return DriverManager.hasActiveDriver();
    }
}
