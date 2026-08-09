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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

        // ===== PHASE 19.4F TEMPORARY DIAGNOSTIC INSTRUMENTATION — REMOVE AFTER INVESTIGATION =====
        // Same class of in-process, read-only capture proven in Phase 19.4E,
        // synchronous with the actual readiness decision below. Does NOT
        // change the readiness condition itself — still exactly
        // `queryAppState(appPackage) == RUNNING_IN_FOREGROUND`, unmodified
        // from Phase 19.4B. See docs/docker/PHASE_19.4F_CONTROLLED_CI_SAMPLING_REPORT.md.
        long diagStartNanos = System.nanoTime();
        AtomicInteger diagPollCounter = new AtomicInteger(0);
        String[] diagLastSignature = {null};
        // ===== END PHASE 19.4F SETUP =====

        try {
            new WebDriverWait(driver, timeout)
                    .until(d -> {
                        ApplicationState state = driver.queryAppState(appPackage);
                        boolean ready = state == ApplicationState.RUNNING_IN_FOREGROUND;

                        // ===== PHASE 19.4F TEMPORARY DIAGNOSTIC INSTRUMENTATION =====
                        logAutReadinessDiagnostic(driver, appPackage, diagPollCounter.incrementAndGet(),
                                diagStartNanos, state, ready, diagLastSignature);
                        // ===== END =====

                        return ready;
                    });
        } catch (TimeoutException e) {
            ApplicationState lastKnownState = driver.queryAppState(appPackage);
            String actualForegroundPackage = safeGetCurrentPackage(driver);

            // ===== PHASE 19.4F TEMPORARY DIAGNOSTIC INSTRUMENTATION =====
            System.out.println("[AUT-READINESS-DIAG] FINAL_TIMEOUT appPackage=" + appPackage
                    + " totalPolls=" + diagPollCounter.get()
                    + " elapsedMs=" + ((System.nanoTime() - diagStartNanos) / 1_000_000)
                    + " lastKnownState=" + lastKnownState
                    + " actualForegroundPackage=" + actualForegroundPackage
                    + " mCurrentFocus=" + safeShellGrep(driver, "dumpsys", List.of("window"), "mCurrentFocus")
                    + " mFocusedApp=" + safeShellGrep(driver, "dumpsys", List.of("window"), "mFocusedApp")
                    + " topResumedActivity=" + safeShellGrep(driver, "dumpsys", List.of("activity", "activities"), "topResumedActivity")
                    + " pid=" + safeShellRaw(driver, "pidof", List.of(appPackage)));
            // ===== END =====

            safeQuit(driver);
            throw new DriverInitializationException(
                    "AUT '" + appPackage + "' did not reach the foreground within " + timeout.getSeconds()
                            + "s after Appium reported session creation successful (last known app state: "
                            + lastKnownState + "; actual foreground package: " + actualForegroundPackage
                            + "). See PHASE_19.4A_DOCKER_CI_INTERMITTENT_FAILURE_FORENSIC_REPORT.md for the verified root cause.",
                    e);
        }
    }

    // ===== PHASE 19.4F TEMPORARY DIAGNOSTIC INSTRUMENTATION — REMOVE AFTER INVESTIGATION =====
    // Logs only on the first poll, on any change in the observed signals, or
    // when readiness resolves — per this phase's own "do not create
    // unnecessary high-volume logs" instruction, not every single poll.
    private void logAutReadinessDiagnostic(AndroidDriver driver, String appPackage, int poll, long startNanos,
                                            ApplicationState state, boolean ready, String[] lastSignature) {
        String currentFocus = safeShellGrep(driver, "dumpsys", List.of("window"), "mCurrentFocus");
        String focusedApp = safeShellGrep(driver, "dumpsys", List.of("window"), "mFocusedApp");
        String resumedActivity = safeShellGrep(driver, "dumpsys", List.of("activity", "activities"), "topResumedActivity");
        String pid = safeShellRaw(driver, "pidof", List.of(appPackage));
        String processState = pid.isBlank() ? "NOT_RUNNING" : ("RUNNING pid=" + pid);

        String signature = state + "|" + currentFocus + "|" + focusedApp + "|" + resumedActivity + "|" + processState;
        boolean changed = !signature.equals(lastSignature[0]);
        lastSignature[0] = signature;

        if (poll == 1 || changed || ready) {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            System.out.println("[AUT-READINESS-DIAG] poll=" + poll
                    + " elapsedMs=" + elapsedMs
                    + " queryAppState=" + state
                    + " ready=" + ready
                    + " appPackage=" + appPackage
                    + " mCurrentFocus=" + currentFocus
                    + " mFocusedApp=" + focusedApp
                    + " topResumedActivity=" + resumedActivity
                    + " processState=" + processState);
        }
    }

    /**
     * Executes a read-only shell command via Appium's official {@code mobile: shell}
     * extension (requires the Appium server's {@code --relaxed-security} flag,
     * already set by the CI workflows exercising this diagnostic) and returns
     * the first line of output containing {@code grepFor}, or {@code "NOT AVAILABLE"}
     * if the signal cannot be retrieved. Never fabricates a value.
     */
    private String safeShellGrep(AndroidDriver driver, String command, List<String> args, String grepFor) {
        try {
            Object result = driver.executeScript("mobile: shell", Map.of("command", command, "args", args));
            return String.valueOf(result).lines()
                    .filter(line -> line.contains(grepFor))
                    .findFirst()
                    .map(String::trim)
                    .orElse("NOT AVAILABLE");
        } catch (RuntimeException e) {
            return "NOT AVAILABLE (" + e.getClass().getSimpleName() + ")";
        }
    }

    private String safeShellRaw(AndroidDriver driver, String command, List<String> args) {
        try {
            Object result = driver.executeScript("mobile: shell", Map.of("command", command, "args", args));
            return String.valueOf(result).trim();
        } catch (RuntimeException e) {
            return "";
        }
    }
    // ===== END PHASE 19.4F TEMPORARY DIAGNOSTIC INSTRUMENTATION =====

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
