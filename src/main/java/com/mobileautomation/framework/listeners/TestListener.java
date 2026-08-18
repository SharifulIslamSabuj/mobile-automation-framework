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
    private static final String DELIMITER = "==========";

    @Override
    public void onTestStart(ITestResult result) {
        LOGGER.info("{} TEST START {}", DELIMITER, DELIMITER);
        LOGGER.info(describeTest(result));
        LOGGER.info("Test started: {}", result.getName());
        ReportProvider.createTest(result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOGGER.info("Test passed: {}", result.getName());
        LOGGER.info("{} TEST END {}", DELIMITER, DELIMITER);
        ReportProvider.clearCurrentTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOGGER.error("Test failed: {}", result.getName(), result.getThrowable());
        ScreenshotManager.captureScreenshot(result.getName() + "_failure");
        // Phase D/E: CommonAssertions.evaluate() is the only other place that marks an
        // ExtentTest failed, and it never runs for a failure raised outside an assertion
        // (e.g. a raw ElementActionException from a Page Object call) — without this, such
        // failures left the Extent node's status as whatever it last was on a PASS, even
        // though TestNG and Allure both correctly recorded FAILURE/broken (docs/allure/
        // PHASE_D_EXTENTREPORTS_FORENSIC_AUDIT_REPORT.md).
        if (ReportProvider.hasActiveTest()) {
            ReportProvider.getTest().fail(result.getThrowable());
        }
        LOGGER.info("{} TEST END {}", DELIMITER, DELIMITER);
        ReportProvider.clearCurrentTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOGGER.warn("Test skipped: {}", result.getName());
        // Phase N: without this, an ExtentTest node left with no explicit status (as every
        // prior code path here did for a skip) renders as PASS in the Spark report — TestNG
        // and Allure both correctly record SKIP/skipped (docs/allure/
        // PHASE_M_GITHUB_ACTIONS_ALLURE_CI_VALIDATION_REPORT.md). getThrowable() is null for
        // the common case (a skip cascading from a failed @BeforeMethod carries no throwable
        // on the test's own ITestResult — confirmed from that run's raw Allure data), so this
        // falls back to a message rather than risk passing a null Throwable to skip(Throwable).
        if (ReportProvider.hasActiveTest()) {
            if (result.getThrowable() != null) {
                ReportProvider.getTest().skip(result.getThrowable());
            } else {
                ReportProvider.getTest().skip("Test skipped: " + result.getName());
            }
        }
        LOGGER.info("{} TEST END {}", DELIMITER, DELIMITER);
        ReportProvider.clearCurrentTest();
    }

    /** Added Phase 9.5I — a human-readable test identifier (its {@code @Test(description=...)}) for the START banner, so a log reader sees "TC-004 — Login Outcome Verification", not just a Java method name. Falls back to the method name if no description was supplied. */
    private String describeTest(ITestResult result) {
        String description = result.getMethod().getDescription();
        return (description != null && !description.isBlank()) ? description : result.getName();
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
