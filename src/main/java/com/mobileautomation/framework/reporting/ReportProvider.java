package com.mobileautomation.framework.reporting;

import com.aventstack.extentreports.ExtentTest;

/**
 * The single, exclusive entry point every other framework layer must use to
 * interact with reporting. No component may reference
 * {@link ExtentReportManager} directly — it is package-private, so the
 * compiler enforces this, not just a naming convention. Mirrors
 * {@code com.mobileautomation.framework.driver.DriverProvider}'s facade
 * pattern.
 */
public final class ReportProvider {

    private ReportProvider() {
    }

    /** Initializes the shared report if it has not been already. Safe to call multiple times/threads. */
    public static void initializeReport() {
        ExtentReportManager.initializeReport();
    }

    /** Creates a new report node for {@code testName} and binds it to the current thread. */
    public static ExtentTest createTest(String testName) {
        return ExtentReportManager.createTest(testName);
    }

    /**
     * @throws com.mobileautomation.framework.exceptions.ReportingException if no test node is bound to the current thread
     */
    public static ExtentTest getTest() {
        return ExtentReportManager.getTest();
    }

    /** @return whether a report node is currently bound to this thread. */
    public static boolean hasActiveTest() {
        return ExtentReportManager.hasActiveTest();
    }

    /** Unbinds the current thread's report node. Safe to call even if none is bound. */
    public static void clearCurrentTest() {
        ExtentReportManager.clearCurrentTest();
    }

    /** Writes the report to disk. No-op if the report was never initialized. */
    public static void flushReport() {
        ExtentReportManager.flushReport();
    }

    /** @return whether the shared report has been initialized. */
    public static boolean isInitialized() {
        return ExtentReportManager.isInitialized();
    }
}
