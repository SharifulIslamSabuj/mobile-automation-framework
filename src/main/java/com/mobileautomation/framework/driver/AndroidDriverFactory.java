package com.mobileautomation.framework.driver;

import com.mobileautomation.framework.config.CapabilityConfiguration;
import com.mobileautomation.framework.config.ConfigReader;
import com.mobileautomation.framework.exceptions.DriverInitializationException;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.appmanagement.ApplicationState;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

/**
 * Creates Android/UiAutomator2 {@link AppiumDriver} sessions.
 * <p>
 * The only implementation of {@link DriverFactory} today, matching the
 * frozen stack (MA-PV-001 §16: Android + UiAutomator2). Consumes the
 * Configuration Layer ({@link ConfigReader}, {@link CapabilityConfiguration})
 * exclusively — it never reads a properties file or a system property
 * directly, and it contains no business logic.
 */
public class AndroidDriverFactory implements DriverFactory {

    private final CapabilityBuilder capabilityBuilder;

    public AndroidDriverFactory() {
        this(new UiAutomator2CapabilityBuilder());
    }

    public AndroidDriverFactory(CapabilityBuilder capabilityBuilder) {
        this.capabilityBuilder = capabilityBuilder;
    }

    @Override
    public AppiumDriver createDriver() {
        ConfigReader configReader = ConfigReader.getInstance();
        CapabilityConfiguration capabilityConfiguration = CapabilityConfiguration.fromConfigReader(configReader);
        Capabilities capabilities = capabilityBuilder.build(capabilityConfiguration);
        URL serverUrl = resolveServerUrl(configReader.getAppiumServerUrl());

        AndroidDriver driver;
        try {
            driver = new AndroidDriver(serverUrl, capabilities);
        } catch (RuntimeException e) {
            throw new DriverInitializationException(
                    "Failed to create Android driver session against Appium server " + serverUrl
                            + ". Capabilities requested: " + capabilities, e);
        }

        verifyAutForegroundReadiness(driver, capabilityConfiguration.getAppPackage(), configReader.getExplicitWaitTimeout());
        return driver;
    }

    /**
     * Appium/UiAutomator2 can report {@code createSession} successful before
     * the AUT has actually reached the foreground — verified directly
     * (docs/docker/PHASE_19.4A_DOCKER_CI_INTERMITTENT_FAILURE_FORENSIC_REPORT.md):
     * the emulator can still be processing its own post-boot BOOT_COMPLETED
     * broadcast fan-out when session creation completes, so the requested
     * app launch has no observable effect (package installed, launcher stays
     * foregrounded, AUT process never starts) while Appium still reports
     * success. Left unguarded, this surfaces ~15 seconds later as an
     * unrelated element-visibility failure in the first test assertion
     * instead of an accurate driver-initialization diagnostic.
     * <p>
     * Uses {@code queryAppState} (an official Appium app-management check,
     * {@link ApplicationState#RUNNING_IN_FOREGROUND}) rather than exact
     * activity matching — the same investigation found activity matching
     * unreliable across this AUT's splash/transition screens, whereas overall
     * foreground app state is the narrowest condition that still proves the
     * AUT, not the launcher, is actually running. A no-op in the healthy case
     * (the same case every prior passing run already exhibited): the poll
     * resolves on its first or second check, well inside the existing
     * explicit-wait budget.
     */
    private void verifyAutForegroundReadiness(AndroidDriver driver, String appPackage, Duration timeout) {
        if (appPackage == null || appPackage.isBlank()) {
            return;
        }

        try {
            new WebDriverWait(driver, timeout)
                    .until(d -> driver.queryAppState(appPackage) == ApplicationState.RUNNING_IN_FOREGROUND);
        } catch (TimeoutException e) {
            ApplicationState lastKnownState = driver.queryAppState(appPackage);
            String actualForegroundPackage = safeGetCurrentPackage(driver);
            safeQuit(driver);
            throw new DriverInitializationException(
                    "AUT '" + appPackage + "' did not reach the foreground within " + timeout.getSeconds()
                            + "s after Appium reported session creation successful (last known app state: "
                            + lastKnownState + "; actual foreground package: " + actualForegroundPackage
                            + "). See PHASE_19.4A_DOCKER_CI_INTERMITTENT_FAILURE_FORENSIC_REPORT.md for the verified root cause.",
                    e);
        }
    }

    private String safeGetCurrentPackage(AndroidDriver driver) {
        try {
            return driver.getCurrentPackage();
        } catch (RuntimeException e) {
            return "<unavailable: " + e.getMessage() + ">";
        }
    }

    private void safeQuit(AndroidDriver driver) {
        try {
            driver.quit();
        } catch (RuntimeException ignored) {
            // Best-effort cleanup of a session that already failed its readiness check;
            // the DriverInitializationException below is the failure that matters.
        }
    }

    private URL resolveServerUrl(String rawUrl) {
        try {
            return URI.create(rawUrl).toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new DriverInitializationException(
                    "Invalid Appium server URL in configuration ('" + com.mobileautomation.framework.constants.ConfigurationKeys.APPIUM_SERVER_URL
                            + "'): '" + rawUrl + "'", e);
        }
    }
}
