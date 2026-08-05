package com.mobileautomation.framework.constants;

/**
 * Compiled-in fallback values used by the Configuration Layer only when a
 * key is absent from both the common and the environment-specific
 * properties file. These are true constants (last-resort defaults), not
 * configurable values — the authoritative value for a given run always
 * comes from the properties files or a system-property override first.
 * <p>
 * Deliberately has zero dependency on the {@code config} package (which
 * depends on this one) to keep the constants→config dependency direction
 * one-way — see {@code Environment.fromProfileName} for how the profile
 * name below is turned into an {@code Environment} enum value.
 */
public final class ConfigurationDefaults {

    private ConfigurationDefaults() {
    }

    // Environment / file resolution
    /** Profile name of the default {@code Environment}, resolved via {@code Environment.fromProfileName(...)}. */
    public static final String DEFAULT_ENVIRONMENT_PROFILE = "emulator";
    public static final String CONFIG_RESOURCE_DIRECTORY = "config/";
    public static final String CONFIG_FILE_PREFIX = "config";
    public static final String CONFIG_FILE_EXTENSION = ".properties";

    // Appium server connection (added Phase 4)
    /** Appium 2.x/3.x server default base URL (no "/wd/hub" suffix — that was Appium 1.x). */
    public static final String DEFAULT_APPIUM_SERVER_URL = "http://127.0.0.1:4723";

    // Device / platform (frozen stack — MA-PV-001 §16: Android + UiAutomator2 only)
    public static final String DEFAULT_PLATFORM_NAME = "Android";
    public static final String DEFAULT_AUTOMATION_NAME = "UiAutomator2";
    public static final String DEFAULT_UDID = "";

    // Driver timeouts
    public static final int DEFAULT_NEW_COMMAND_TIMEOUT_SECONDS = 120;
    /** Per MA-FR-001 §5 (explicit waits only), implicit wait defaults to disabled. */
    public static final int DEFAULT_IMPLICIT_WAIT_SECONDS = 0;
    public static final int DEFAULT_EXPLICIT_WAIT_TIMEOUT_SECONDS = 15;
    public static final int DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS = 30;

    // Execution
    // Added Phase 9.5I — see config.ExecutionStrategy; ISOLATED (clean AUT state
    // guaranteed per test) is the enterprise-default, replacing the former
    // DEFAULT_NO_RESET=true default that allowed state to persist.
    public static final String DEFAULT_EXECUTION_STRATEGY = "ISOLATED";
    public static final boolean DEFAULT_FULL_RESET = false;
    public static final boolean DEFAULT_AUTO_GRANT_PERMISSIONS = true;
    public static final int DEFAULT_RETRY_COUNT = 1;

    // Reporting
    public static final String DEFAULT_REPORT_DIRECTORY = "reports";
    public static final String DEFAULT_SCREENSHOT_DIRECTORY = "reports/screenshots";

    // Logging
    public static final String DEFAULT_LOG_LEVEL = "INFO";
    /** Added Phase 6. */
    public static final String DEFAULT_LOGGING_DIRECTORY = "logs";

    // Test data resource layout (added Phase 8) — a compiled-in classpath layout
    // convention, exactly like CONFIG_RESOURCE_DIRECTORY above; the part that
    // actually varies per run (which environment folder) comes from
    // ConfigReader.getEnvironment(), not from here.
    public static final String TESTDATA_RESOURCE_DIRECTORY = "testdata/";
    public static final String TESTDATA_COMMON_FOLDER = "common/";
}
