package com.mobileautomation.framework.assertions;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.mobileautomation.framework.logging.LogManager;
import com.mobileautomation.framework.reporting.ReportProvider;
import com.mobileautomation.framework.reporting.ScreenshotManager;
import org.slf4j.Logger;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Reusable, generic assertion wrappers — no business assertions. Every
 * method takes an already-computed actual value (a boolean state or a
 * string), never a locator or a driver, keeping this class fully decoupled
 * from {@code core.ElementActions}: obtaining the actual value is the
 * caller's (a future Page Object's) responsibility, asserting on it is
 * this class's. Assertion messages are centralized through one private
 * {@code evaluate} method so every failure is logged, reported, and
 * screenshotted identically.
 * <p>
 * Failures throw a standard {@link AssertionError} — not a
 * {@code FrameworkException} subtype — so TestNG's native pass/fail
 * detection continues to work exactly as it does for {@code org.testng.Assert}.
 */
public final class CommonAssertions {

    private static final Logger LOGGER = LogManager.getLogger(CommonAssertions.class);

    private CommonAssertions() {
    }

    public static void verifyVisible(boolean isVisible, String elementName) {
        evaluate(isVisible, "verifyVisible", elementName, "expected to be visible");
    }

    public static void verifyHidden(boolean isVisible, String elementName) {
        evaluate(!isVisible, "verifyHidden", elementName, "expected to be hidden");
    }

    public static void verifyEnabled(boolean isEnabled, String elementName) {
        evaluate(isEnabled, "verifyEnabled", elementName, "expected to be enabled");
    }

    public static void verifyDisabled(boolean isEnabled, String elementName) {
        evaluate(!isEnabled, "verifyDisabled", elementName, "expected to be disabled");
    }

    public static void verifyText(String actualText, String expectedText, String elementName) {
        boolean passed = expectedText == null ? actualText == null : expectedText.equals(actualText);
        evaluate(passed, "verifyText", elementName,
                "expected text '" + expectedText + "' but found '" + actualText + "'");
    }

    public static void verifyContains(String actualText, String expectedSubstring, String elementName) {
        boolean passed = actualText != null && expectedSubstring != null && actualText.contains(expectedSubstring);
        evaluate(passed, "verifyContains", elementName,
                "expected '" + actualText + "' to contain '" + expectedSubstring + "'");
    }

    private static void evaluate(boolean passed, String assertionType, String elementName, String detail) {
        String message = assertionType + " [" + elementName + "]: " + detail;

        if (passed) {
            LOGGER.info("Assertion passed: {}", message);
            if (ReportProvider.hasActiveTest()) {
                ReportProvider.getTest().pass(message);
            }
            Allure.step(message, Status.PASSED);
            return;
        }

        LOGGER.error("Assertion failed: {}", message);
        Optional<Path> screenshot = ScreenshotManager.captureScreenshot("assertion_" + assertionType + "_failure");
        if (ReportProvider.hasActiveTest()) {
            ExtentTest test = ReportProvider.getTest();
            if (screenshot.isPresent()) {
                test.fail(message, MediaEntityBuilder.createScreenCaptureFromPath(screenshot.get().toString()).build());
            } else {
                test.fail(message);
            }
        }
        Allure.step(message, Status.FAILED);
        throw new AssertionError(message);
    }
}
