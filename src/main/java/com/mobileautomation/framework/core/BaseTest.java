package com.mobileautomation.framework.core;

import com.mobileautomation.framework.config.ConfigReader;
import com.mobileautomation.framework.driver.DriverProvider;
import com.mobileautomation.framework.listeners.MethodListener;
import com.mobileautomation.framework.listeners.SuiteListener;
import com.mobileautomation.framework.listeners.TestListener;
import com.mobileautomation.framework.logging.LogManager;
import org.slf4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

/**
 * Reusable TestNG lifecycle support every future Test Class inherits.
 * Contains no {@code @Test} method, no test logic, and no application
 * logic — only driver init/cleanup around each test method and listener
 * registration. Report initialization is not duplicated here: it is
 * already triggered by {@link SuiteListener#onStart}, which this class
 * registers via {@link Listeners}.
 * <p>
 * Driver lifecycle is scoped per test <b>method</b> (not per class) for
 * full test isolation — see the Architectural Decisions section of
 * docs/framework/BASE_FRAMEWORK_ARCHITECTURE.md for the tradeoff this
 * implies and why it was chosen as the default.
 */
@Listeners({SuiteListener.class, TestListener.class, MethodListener.class})
public abstract class BaseTest {

    protected final Logger logger = LogManager.getLogger(getClass());

    @BeforeMethod(alwaysRun = true)
    public void initializeDriver() {
        logger.info("Initializing driver for test method. Execution Strategy: {} (noReset={}).",
                config().getExecutionStrategy(), config().isNoReset());
        DriverProvider.initializeDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void quitDriver() {
        logger.info("Quitting driver for test method.");
        DriverProvider.quitDriver();
    }

    protected ConfigReader config() {
        return ConfigReader.getInstance();
    }
}
