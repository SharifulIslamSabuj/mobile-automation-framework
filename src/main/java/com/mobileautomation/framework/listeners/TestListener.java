package com.mobileautomation.framework.listeners;

import com.mobileautomation.framework.logging.LogManager;
import com.mobileautomation.framework.reporting.ReportProvider;
import com.mobileautomation.framework.reporting.ScreenshotManager;
import org.slf4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Drives the report/logging/screenshot lifecycle at test-method granularity.
 * Contains no Page Object or business-assertion logic — it only observes
 * {@link ITestResult} and delegates to the reporting and screenshot
 * infrastructure built in this phase.
 */
public class TestListener implements ITestListener {

    private static final Logger LOGGER = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        LOGGER.info("Test started: {}", result.getName());
        ReportProvider.createTest(result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOGGER.info("Test passed: {}", result.getName());
        ReportProvider.clearCurrentTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOGGER.error("Test failed: {}", result.getName(), result.getThrowable());
        ScreenshotManager.captureScreenshot(result.getName() + "_failure");
        ReportProvider.clearCurrentTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOGGER.warn("Test skipped: {}", result.getName());
        ReportProvider.clearCurrentTest();
    }

    @Override
    public void onStart(ITestContext context) {
        LOGGER.info("Test context started: {}", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        LOGGER.info("Test context finished: {}", context.getName());
    }
}
