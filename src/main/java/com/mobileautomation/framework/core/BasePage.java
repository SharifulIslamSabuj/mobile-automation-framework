package com.mobileautomation.framework.core;

import com.mobileautomation.framework.driver.DriverProvider;
import com.mobileautomation.framework.logging.LogManager;
import com.mobileautomation.framework.reporting.ScreenshotManager;
import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Common functionality every future Page Object inherits. Deliberately
 * lightweight — no business workflows, no application navigation, no
 * assertions, no test logic. Provides only: driver access, element
 * interaction delegation, navigation helper access, screenshot helper
 * access, and logging access.
 * <p>
 * Does not manage driver lifecycle (no {@code initializeDriver}/{@code quitDriver}
 * call anywhere in this class) — that remains {@link com.mobileautomation.framework.driver.DriverProvider}'s
 * and, at the test-execution level, {@link BaseTest}'s responsibility.
 */
public abstract class BasePage {

    protected final Logger logger = LogManager.getLogger(getClass());
    protected final ElementActions elementActions = new ElementActions();
    protected final NavigationHelper navigationHelper = new NavigationHelper();

    /** Raw driver access for the rare case a capability isn't already wrapped by {@link #elementActions} or {@link #navigationHelper}. */
    protected AppiumDriver driver() {
        return (AppiumDriver) DriverProvider.getDriver();
    }

    protected Optional<Path> captureScreenshot(String namePrefix) {
        return ScreenshotManager.captureScreenshot(namePrefix);
    }
}
