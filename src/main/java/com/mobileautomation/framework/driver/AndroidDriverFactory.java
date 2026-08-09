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

            // ===== PHASE 19.4G TEMPORARY DIAGNOSTIC INSTRUMENTATION — REMOVE AFTER INVESTIGATION =====
            // T0 = this exact moment (the readiness check has just reported
            // success). Starts a background, read-only sampler observing
            // device/Appium state continuously for a bounded window following
            // T0, on its own daemon thread so it cannot delay or block the
            // actual test flow. See
            // docs/docker/PHASE_19.4G_CONTINUOUS_FAILURE_WINDOW_FORENSIC_REPORT.md.
            startContinuousWindowDiagnostic(driver, appPackage);
            // ===== END =====
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

    // ===== PHASE 19.4G TEMPORARY DIAGNOSTIC INSTRUMENTATION — REMOVE AFTER INVESTIGATION =====
    // Continuous, read-only, background sampling from T0 (readiness success)
    // through a bounded window (~15.2s, comfortably inside the explicit-wait
    // budget every prior run's test body has run within, whether it passes
    // quickly or times out at 15s) — designed to survive the daemon thread
    // being killed at any point without warning if the JVM exits early (a
    // passing run's suite can tear down in a few seconds): state signals are
    // logged only on change/heartbeat (not every poll), and logcat is dumped
    // cumulatively at checkpoints rather than only once at the very end, so
    // whatever the thread survives to see is still usable evidence.
    private static final long WINDOW_DIAG_DURATION_MS = 15_200;
    private static final long WINDOW_DIAG_POLL_INTERVAL_MS = 700;
    private static final long WINDOW_DIAG_LOGCAT_CHECKPOINT_MS = 5_000;

    private void startContinuousWindowDiagnostic(AndroidDriver driver, String appPackage) {
        Thread diagnosticThread = new Thread(
                () -> runContinuousWindowDiagnostic(driver, appPackage), "phase19-4g-window-diag");
        diagnosticThread.setDaemon(true);
        diagnosticThread.start();
    }

    private void runContinuousWindowDiagnostic(AndroidDriver driver, String appPackage) {
        long windowStartNanos = System.nanoTime();
        safeShellRaw(driver, "logcat", List.of("-c"));
        System.out.println("[AUT-WINDOW-DIAG] WINDOW_START appPackage=" + appPackage);

        String lastSignature = null;
        int sample = 0;
        long nextLogcatCheckpointMs = WINDOW_DIAG_LOGCAT_CHECKPOINT_MS;
        long elapsedMs = 0;

        while (elapsedMs(windowStartNanos) < WINDOW_DIAG_DURATION_MS) {
            sample++;
            elapsedMs = elapsedMs(windowStartNanos);

            try {
                String queryState = safeQueryAppState(driver, appPackage);
                String windowDump = safeShellRaw(driver, "dumpsys", List.of("window"));
                String currentFocus = extractLine(windowDump, "mCurrentFocus");
                String focusedApp = extractLine(windowDump, "mFocusedApp");
                String activityDump = safeShellRaw(driver, "dumpsys", List.of("activity", "activities"));
                String resumedActivityAM = extractLine(activityDump, "mResumedActivity");
                String topResumedActivity = extractLine(activityDump, "topResumedActivity");
                String pid = safeShellRaw(driver, "pidof", List.of(appPackage));
                String processState = (pid == null || pid.isBlank()) ? "NOT_RUNNING" : ("RUNNING pid=" + pid);
                String currentPackageViaAppium = safeGetCurrentPackage(driver);
                String pageSourceRootPackage = safePageSourceRootPackage(driver);

                String signature = queryState + "|" + currentFocus + "|" + focusedApp + "|" + resumedActivityAM
                        + "|" + topResumedActivity + "|" + processState + "|" + currentPackageViaAppium
                        + "|" + pageSourceRootPackage;
                boolean changed = !signature.equals(lastSignature);
                lastSignature = signature;
                boolean heartbeat = sample % 7 == 0;

                if (sample == 1 || changed || heartbeat) {
                    System.out.println("[AUT-WINDOW-DIAG] t=+" + elapsedMs + "ms sample=" + sample
                            + " queryAppState=" + queryState
                            + " mCurrentFocus=" + currentFocus
                            + " mFocusedApp=" + focusedApp
                            + " mResumedActivity=" + resumedActivityAM
                            + " topResumedActivity=" + topResumedActivity
                            + " processState=" + processState
                            + " currentPackageViaAppium=" + currentPackageViaAppium
                            + " pageSourceRootPackage=" + pageSourceRootPackage
                            + (changed ? " CHANGED" : (heartbeat ? " HEARTBEAT" : "")));
                }

                if (elapsedMs >= nextLogcatCheckpointMs) {
                    dumpLogcatCheckpoint(driver, appPackage, elapsedMs);
                    nextLogcatCheckpointMs += WINDOW_DIAG_LOGCAT_CHECKPOINT_MS;
                }
            } catch (RuntimeException sampleError) {
                System.out.println("[AUT-WINDOW-DIAG] t=+" + elapsedMs + "ms sample=" + sample
                        + " SAMPLE_ERROR=" + sampleError.getClass().getSimpleName());
            }

            try {
                Thread.sleep(WINDOW_DIAG_POLL_INTERVAL_MS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        dumpLogcatCheckpoint(driver, appPackage, elapsedMs(windowStartNanos));
        System.out.println("[AUT-WINDOW-DIAG] WINDOW_END appPackage=" + appPackage + " totalSamples=" + sample);
    }

    private long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private String safeQueryAppState(AndroidDriver driver, String appPackage) {
        try {
            return String.valueOf(driver.queryAppState(appPackage));
        } catch (RuntimeException e) {
            return "ERROR(" + e.getClass().getSimpleName() + ")";
        }
    }

    private String safePageSourceRootPackage(AndroidDriver driver) {
        try {
            String pageSource = driver.getPageSource();
            int idx = pageSource.indexOf("package=\"");
            if (idx < 0) {
                return "NOT AVAILABLE";
            }
            int start = idx + "package=\"".length();
            int end = pageSource.indexOf('"', start);
            return end < 0 ? "NOT AVAILABLE" : pageSource.substring(start, end);
        } catch (RuntimeException e) {
            return "ERROR(" + e.getClass().getSimpleName() + ")";
        }
    }

    private String extractLine(String dump, String grepFor) {
        if (dump == null) {
            return "NOT AVAILABLE (shell error)";
        }
        return dump.lines()
                .filter(line -> line.contains(grepFor))
                .findFirst()
                .map(String::trim)
                .orElse("NOT AVAILABLE");
    }

    private void dumpLogcatCheckpoint(AndroidDriver driver, String appPackage, long elapsedMs) {
        String tagFiltered = safeShellRaw(driver, "sh", List.of("-c",
                "logcat -d -v time ActivityTaskManager:I ActivityManager:I WindowManager:I InputDispatcher:I AndroidRuntime:E *:S"));
        String packageGrep = safeShellRaw(driver, "sh", List.of("-c", "logcat -d | grep -i " + appPackage));
        System.out.println("[AUT-LOGCAT-DIAG] checkpoint=+" + elapsedMs + "ms TAG_FILTERED_BEGIN");
        System.out.println(truncate(tagFiltered, 6000));
        System.out.println("[AUT-LOGCAT-DIAG] checkpoint=+" + elapsedMs + "ms TAG_FILTERED_END");
        System.out.println("[AUT-LOGCAT-DIAG] checkpoint=+" + elapsedMs + "ms PACKAGE_GREP_BEGIN");
        System.out.println(truncate(packageGrep, 6000));
        System.out.println("[AUT-LOGCAT-DIAG] checkpoint=+" + elapsedMs + "ms PACKAGE_GREP_END");
    }

    private String truncate(String text, int maxChars) {
        if (text == null) {
            return "NOT AVAILABLE (shell error)";
        }
        return text.length() <= maxChars ? text : text.substring(0, maxChars) + "...[TRUNCATED]";
    }

    private String safeShellRaw(AndroidDriver driver, String command, List<String> args) {
        try {
            Object result = driver.executeScript("mobile: shell", Map.of("command", command, "args", args));
            return String.valueOf(result).trim();
        } catch (RuntimeException e) {
            return null;
        }
    }
    // ===== END PHASE 19.4G TEMPORARY DIAGNOSTIC INSTRUMENTATION =====

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
