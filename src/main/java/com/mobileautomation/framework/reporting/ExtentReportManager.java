package com.mobileautomation.framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.mobileautomation.framework.config.ConfigReader;
import com.mobileautomation.framework.exceptions.ReportingException;
import com.mobileautomation.framework.logging.LogManager;
import com.mobileautomation.framework.utils.DateUtility;
import com.mobileautomation.framework.utils.FileUtility;
import com.mobileautomation.framework.utils.RandomUtility;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Owns the single {@link ExtentReports} instance for the whole JVM and the
 * per-thread {@link ExtentTest} currently in progress. Package-private,
 * mirroring the {@code DriverManager}/{@code DriverProvider} split — callers
 * outside this package go through {@link ReportProvider}.
 * <p>
 * This phase prepares reporting infrastructure only: initialization,
 * lifecycle, and thread-safe test-node management. No test result is logged
 * to the report here — that begins once listeners/business logic call these
 * APIs in a later phase.
 */
final class ExtentReportManager {

    private static final Logger LOGGER = LogManager.getLogger(ExtentReportManager.class);
    private static final String REPORT_FILE_PREFIX = "AutomationReport_";
    private static final String REPORT_FILE_EXTENSION = ".html";
    private static final String REPORT_TITLE = "Mobile Automation Report";
    private static final String REPORT_NAME = "Sauce Labs My Demo App - Android";

    private static volatile ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();

    private ExtentReportManager() {
    }

    static void initializeReport() {
        if (extentReports != null) {
            return;
        }
        synchronized (ExtentReportManager.class) {
            if (extentReports != null) {
                return;
            }
            extentReports = buildExtentReports();
            LOGGER.info("Extent report initialized.");
        }
    }

    private static ExtentReports buildExtentReports() {
        ConfigReader configReader = ConfigReader.getInstance();
        Path reportDirectory = FileUtility.createDirectoryIfNotExists(
                FileUtility.buildPath(configReader.getReportDirectory()));
        String fileName = REPORT_FILE_PREFIX + DateUtility.timestampForFilename() + "_"
                + RandomUtility.randomAlphanumeric(6) + REPORT_FILE_EXTENSION;
        Path reportFile = reportDirectory.resolve(fileName);

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportFile.toString());
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle(REPORT_TITLE);
        sparkReporter.config().setReportName(REPORT_NAME);

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(sparkReporter);
        reports.setSystemInfo("Environment", configReader.getEnvironment().name());
        reports.setSystemInfo("Platform", configReader.getPlatformName());
        reports.setSystemInfo("Automation Engine", configReader.getAutomationName());
        reports.setSystemInfo("Execution Mode", configReader.getExecutionMode().name());
        return reports;
    }

    static ExtentTest createTest(String testName) {
        initializeReport();
        ExtentTest test = extentReports.createTest(testName);
        CURRENT_TEST.set(test);
        return test;
    }

    static ExtentTest getTest() {
        ExtentTest test = CURRENT_TEST.get();
        if (test == null) {
            throw new ReportingException("No active ExtentTest on thread '" + Thread.currentThread().getName()
                    + "'. createTest(...) must be called before getTest().");
        }
        return test;
    }

    static boolean hasActiveTest() {
        return CURRENT_TEST.get() != null;
    }

    static void clearCurrentTest() {
        CURRENT_TEST.remove();
    }

    static void flushReport() {
        if (extentReports == null) {
            return;
        }
        extentReports.flush();
        LOGGER.info("Extent report flushed.");
    }

    static boolean isInitialized() {
        return extentReports != null;
    }
}
