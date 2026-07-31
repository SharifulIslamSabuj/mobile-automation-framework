package com.mobileautomation.framework.driver;

import com.mobileautomation.framework.exceptions.DriverInitializationException;
import io.appium.java_client.AppiumDriver;

/**
 * Centralizes Appium driver lifecycle using a {@link ThreadLocal}: one
 * driver per execution thread, ready for parallel test execution without
 * any change to this class.
 * <p>
 * <b>Package-private by design.</b> No class outside the {@code driver}
 * package may reference this class — {@link DriverProvider} is the only
 * permitted access point for every other framework layer. This is enforced
 * by the compiler (package-private visibility), not just documented as a
 * convention.
 */
final class DriverManager {

    /**
     * Deliberately a plain {@link ThreadLocal}, not {@code InheritableThreadLocal}:
     * a worker thread spawned during parallel execution must initialize its
     * own driver, never silently inherit its parent's.
     */
    private static final ThreadLocal<AppiumDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    /**
     * Initializes a driver on the current thread using the default
     * {@link AndroidDriverFactory}. Safe to call repeatedly: if a driver is
     * already active on this thread, this is a no-op — it never creates a
     * duplicate session.
     */
    static void initializeDriver() {
        initializeDriver(new AndroidDriverFactory());
    }

    /**
     * Initializes a driver on the current thread using the given
     * {@link DriverFactory}. Safe to call repeatedly (see {@link #initializeDriver()}).
     */
    static void initializeDriver(DriverFactory driverFactory) {
        if (DRIVER.get() != null) {
            return;
        }
        DRIVER.set(driverFactory.createDriver());
    }

    /**
     * @throws DriverInitializationException if no driver has been initialized on the current thread
     */
    static AppiumDriver getDriver() {
        AppiumDriver driver = DRIVER.get();
        if (driver == null) {
            throw new DriverInitializationException(
                    "No driver has been initialized on thread '" + Thread.currentThread().getName()
                            + "'. Call DriverProvider.initializeDriver() first.");
        }
        return driver;
    }

    /**
     * Quits the driver active on the current thread, if any, and always
     * clears the {@link ThreadLocal} slot afterward — even if {@code quit()}
     * itself throws — so a failed quit can never leak a stale reference into
     * a later test that reuses this thread. A safe no-op if no driver is
     * active on this thread (prevents double-quit errors).
     */
    static void quitDriver() {
        AppiumDriver driver = DRIVER.get();
        if (driver == null) {
            return;
        }
        try {
            driver.quit();
        } finally {
            DRIVER.remove();
        }
    }

    static boolean hasActiveDriver() {
        return DRIVER.get() != null;
    }
}
